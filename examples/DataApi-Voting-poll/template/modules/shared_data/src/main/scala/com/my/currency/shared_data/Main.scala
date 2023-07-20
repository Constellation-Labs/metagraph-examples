package com.my.currency.shared_data

import cats.data.{NonEmptyList, NonEmptySet}
import cats.effect.IO
import com.my.currency.shared_data.Errors.{ClosedPool, CouldNotGetLatestCurrencySnapshot, CouldNotGetLatestState, InvalidAddress, InvalidEndSnapshot, InvalidOption, PollAlreadyExists, PollDoesNotExists, RepeatedVote}
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, parser}
import io.circe.syntax.EncoderOps
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataApplicationValidationError, DataState, DataUpdate, L1NodeContext}
import org.tessellation.security.signature.Signed
import cats.syntax.all._
import com.my.currency.shared_data.Combiners.{combineCreatePoll, combineVoteInPoll}
import com.my.currency.shared_data.Data.{CreatePollUpdate, Poll, PollState, PollUpdate, VoteInPollUpdate, State}
import com.my.currency.shared_data.Validations.{createPollValidationsWithSignature, createPollValidations, voteInPollValidationsWithSignature, voteInPollValidations}
import com.my.currency.shared_data.TypeValidators.{validateIfOptionExists, validateIfPollAlreadyExists, validateIfUserAlreadyVoted, validateIfVotePollExists, validatePollSnapshotInterval, validateProvidedAddress, validateSnapshotCreatePoll}
import com.my.currency.shared_data.Utils.{customStateDeserialization, customStateSerialization, customUpdateDeserialization, customUpdateSerialization}
import monocle.syntax.all._
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.address.Address
import org.tessellation.security.SecurityProvider
import org.tessellation.security.hash.Hash
import org.tessellation.security.signature.signature.SignatureProof

import java.nio.charset.StandardCharsets

object Errors {
  case object PollAlreadyExists extends DataApplicationValidationError {
    val message = "Poll already exists"
  }

  case object PollDoesNotExists extends DataApplicationValidationError {
    val message = "Poll does not exists"
  }

  case object InvalidOption extends DataApplicationValidationError {
    val message = "Invalid option"
  }

  case object InvalidAddress extends DataApplicationValidationError {
    val message = "Provided address different than proof"
  }

  case object RepeatedVote extends DataApplicationValidationError {
    val message = "This user already voted!"
  }

  case object InvalidEndSnapshot extends DataApplicationValidationError {
    val message = "Provided end snapshot ordinal lower than current snapshot!"
  }

  case object CouldNotGetLatestCurrencySnapshot extends DataApplicationValidationError {
    val message = "Could not get latest currency snapshot!"
  }

  case object CouldNotGetLatestState extends DataApplicationValidationError {
    val message = "Could not get latest state!"
  }

  case object ClosedPool extends DataApplicationValidationError {
    val message = "Pool is closed"
  }
}

object TypeValidators {
  def validateIfPollAlreadyExists(maybeState: Option[PollState]): DataApplicationValidationErrorOr[Unit] = {
    maybeState match {
      case Some(_) => PollAlreadyExists.asInstanceOf[DataApplicationValidationError].invalidNec
      case None => ().validNec
    }
  }

  def validateIfVotePollExists(maybeState: Option[PollState]): DataApplicationValidationErrorOr[Unit] = {
    maybeState match {
      case Some(_) => ().validNec
      case None => PollDoesNotExists.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }

  def validateIfOptionExists(maybeState: Option[PollState], option: String): DataApplicationValidationErrorOr[Unit] = {
    maybeState match {
      case Some(value) =>
        val pollValidOptions = value.pollOptions
        pollValidOptions.get(option) match {
          case Some(_) => ().validNec
          case None => InvalidOption.asInstanceOf[DataApplicationValidationError].invalidNec
        }
      case None => InvalidOption.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }

  def validateProvidedAddress(proofAddresses: List[Address], address: Address): DataApplicationValidationErrorOr[Unit] = {
    if (proofAddresses.contains(address)) {
      ().validNec
    } else {
      InvalidAddress.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }

  def validateIfUserAlreadyVoted(maybeState: Option[PollState], address: Address): DataApplicationValidationErrorOr[Unit] = {
    maybeState match {
      case Some(value) =>
        val pollVotes = value.votes
        pollVotes.get(address) match {
          case Some(_) => RepeatedVote.asInstanceOf[DataApplicationValidationError].invalidNec
          case None => ().validNec
        }
      case None => ().validNec
    }
  }

  def validateSnapshotCreatePoll(snapshotOrdinal: SnapshotOrdinal, update: CreatePollUpdate): DataApplicationValidationErrorOr[Unit] = {
    if (update.endSnapshotOrdinal < snapshotOrdinal.value.value) {
      InvalidEndSnapshot.asInstanceOf[DataApplicationValidationError].invalidNec
    } else {
      ().validNec
    }
  }

  def validatePollSnapshotInterval(lastSnapshotOrdinal: SnapshotOrdinal, currentState: State, voteInPoll: VoteInPollUpdate): DataApplicationValidationErrorOr[Unit] = {
    val poll = currentState.polls.get(voteInPoll.pollId)
    poll match {
      case Some(value) =>
        if (value.endSnapshotOrdinal < lastSnapshotOrdinal.value.value) {
          ClosedPool.asInstanceOf[DataApplicationValidationError].invalidNec
        } else {
          ().validNec
        }
      case None => PollDoesNotExists.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }
}

object Validations {
  def createPollValidations(update: CreatePollUpdate, state: State, lastSnapshotOrdinal: Option[SnapshotOrdinal]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateCreatePollSnapshot = IO {
      lastSnapshotOrdinal match {
        case Some(value) => validateSnapshotCreatePoll(value, update)
        case None => ().validNec
      }
    }

    val validatePoll = IO {
      val voteId = Hash.fromBytes(customUpdateSerialization(update))
      validateIfPollAlreadyExists(state.polls.get(voteId.toString))
    }

    for {
      validatedCreatePollSnapshot <- validateCreatePollSnapshot
      validatedPoll <- validatePoll
    } yield validatedCreatePollSnapshot.productR(validatedPoll)

  }

  def voteInPollValidations(update: VoteInPollUpdate, state: State, lastSnapshotOrdinal: Option[SnapshotOrdinal]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateSnapshotInterval = IO {
      lastSnapshotOrdinal match {
        case Some(value) => validatePollSnapshotInterval(value, state, update)
        case None => ().validNec
      }
    }

    val validatePoll = IO {
      validateIfVotePollExists(state.polls.get(update.pollId))
    }

    val validateOption = IO {
      validateIfOptionExists(state.polls.get(update.pollId), update.option)
    }

    val validateRepeatedVote = IO {
      validateIfUserAlreadyVoted(state.polls.get(update.pollId), update.address)
    }

    for {
      validatedSnapshotInterval <- validateSnapshotInterval
      validatedPoll <- validatePoll
      validatedOption <- validateOption
      validatedRepeatedVote <- validateRepeatedVote
    } yield validatedSnapshotInterval.productR(validatedPoll).productR(validatedOption).productR(validatedRepeatedVote)

  }

  def createPollValidationsWithSignature(update: CreatePollUpdate, proofs: NonEmptySet[SignatureProof], state: State)(implicit sp: SecurityProvider[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddress = proofs
      .map(_.id)
      .toList
      .traverse(_.toAddress[IO])
      .map(validateProvidedAddress(_, update.owner))

    val validations = createPollValidations(update, state, None)
    for {
      validatedAddress <- validateAddress
      validatedPoll <- validations
    } yield validatedAddress.productR(validatedPoll)
  }

  def voteInPollValidationsWithSignature(update: VoteInPollUpdate, proofs: NonEmptySet[SignatureProof], state: State)(implicit sp: SecurityProvider[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddress = proofs
      .map(_.id)
      .toList
      .traverse(_.toAddress[IO])
      .map(validateProvidedAddress(_, update.address))

    val validations = voteInPollValidations(update, state, None)

    for {
      validatedAddress <- validateAddress
      validatedPoll <- validations
    } yield validatedAddress.productR(validatedPoll)
  }
}

object Combiners {
  def combineCreatePoll(poll: CreatePollUpdate, acc: State): State = {
    val pollId = Hash.fromBytes(customUpdateSerialization(poll)).toString
    val pollOptions = poll.pollOptions.flatMap(option => Map(option -> 0L)).toMap
    val newState = Poll(pollId, poll.name, poll.owner, pollOptions, Map.empty, poll.startSnapshotOrdinal, poll.endSnapshotOrdinal)

    acc.focus(_.polls).modify(_.updated(pollId, newState))
  }

  def combineVoteInPoll(voteInPoll: VoteInPollUpdate, acc: State): State = {
    val currentState = acc.polls(voteInPoll.pollId)
    val currentOptionNumber = currentState.pollOptions(voteInPoll.option)

    val newState = currentState
      .focus(_.pollOptions)
      .modify(_.updated(voteInPoll.option, currentOptionNumber + 1))
      .focus(_.votes)
      .modify(_.updated(voteInPoll.address, Map(voteInPoll.option -> 1)))

    acc.focus(_.polls).modify(_.updated(voteInPoll.pollId, newState))
  }
}

object Utils {
  def customUpdateSerialization(update: PollUpdate): Array[Byte] = {
    println("Serialize UPDATE event received")
    println(update.asJson.deepDropNullValues.noSpaces)
    update.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
  }

  def customStateSerialization(state: State): Array[Byte] = {
    println("Serialize STATE event received")
    println(state.asJson.deepDropNullValues.noSpaces)
    state.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
  }

  def customStateDeserialization(bytes: Array[Byte]): Either[Throwable, State] = {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[State]
    }
  }

  def customUpdateDeserialization(bytes: Array[Byte]): Either[Throwable, PollUpdate] = {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[PollUpdate]
    }
  }
}

object Data {
  @derive(decoder, encoder)
  sealed trait PollUpdate extends DataUpdate

  @derive(decoder, encoder)
  case class CreatePollUpdate(name: String, owner: Address, pollOptions: List[String], startSnapshotOrdinal: Long, endSnapshotOrdinal: Long) extends PollUpdate

  @derive(decoder, encoder)
  case class VoteInPollUpdate(pollId: String, address: Address, option: String) extends PollUpdate

  @derive(decoder, encoder)
  sealed trait PollState extends DataState {
    val name: String
    val owner: Address
    val pollOptions: Map[String, Long]
    val votes: Map[Address, Map[String, Long]]
    val startSnapshotOrdinal: Long
    val endSnapshotOrdinal: Long
  }

  @derive(decoder, encoder)
  case class Poll(id: String, name: String, owner: Address, pollOptions: Map[String, Long], votes: Map[Address, Map[String, Long]], startSnapshotOrdinal: Long, endSnapshotOrdinal: Long) extends PollState

  @derive(decoder, encoder)
  case class State(polls: Map[String, Poll]) extends DataState

  def validateUpdate(update: PollUpdate)(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val lastCurrencySnapshot = context.getLastCurrencySnapshot
    lastCurrencySnapshot.map(_.get.data).flatMap {
      case Some(state) =>
        val currentState = customStateDeserialization(state)
        currentState match {
          case Left(_) => IO {
            CouldNotGetLatestState.asInstanceOf[DataApplicationValidationError].invalidNec
          }
          case Right(state) =>
            lastCurrencySnapshot.map(_.get.ordinal).flatMap { lastSnapshotOrdinal =>
              update match {
                case poll: CreatePollUpdate =>
                  createPollValidations(poll, state, Some(lastSnapshotOrdinal))
                case voteInPoll: VoteInPollUpdate =>
                  voteInPollValidations(voteInPoll, state, Some(lastSnapshotOrdinal))
              }
            }
        }
      case None => IO {
        CouldNotGetLatestCurrencySnapshot.asInstanceOf[DataApplicationValidationError].invalidNec
      }
    }
  }

  def validateData(oldState: State, updates: NonEmptyList[Signed[PollUpdate]])(implicit sp: SecurityProvider[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    updates.traverse { signedUpdate =>
      signedUpdate.value match {
        case poll: CreatePollUpdate =>
          createPollValidationsWithSignature(poll, signedUpdate.proofs, oldState)
        case pollUpdate: VoteInPollUpdate =>
          voteInPollValidationsWithSignature(pollUpdate, signedUpdate.proofs, oldState)
      }
    }.map(_.reduce)
  }

  def combine(oldState: State, updates: NonEmptyList[Signed[PollUpdate]]): IO[State] = IO {
    updates.foldLeft(oldState) { (acc, signedUpdate) => {
      val update = signedUpdate.value
      update match {
        case poll: CreatePollUpdate =>
          combineCreatePoll(poll, acc)
        case voteInPoll: VoteInPollUpdate =>
          combineVoteInPoll(voteInPoll, acc)

      }
    }
    }
  }

  def serializeState(state: State): IO[Array[Byte]] = IO {
    customStateSerialization(state)
  }

  def deserializeState(bytes: Array[Byte]): IO[Either[Throwable, State]] = IO {
    customStateDeserialization(bytes)
  }

  def serializeUpdate(update: PollUpdate): IO[Array[Byte]] = IO {
    customUpdateSerialization(update)
  }

  def deserializeUpdate(bytes: Array[Byte]): IO[Either[Throwable, PollUpdate]] = IO {
    customUpdateDeserialization(bytes)
  }

  def dataEncoder: Encoder[PollUpdate] = deriveEncoder

  def dataDecoder: Decoder[PollUpdate] = deriveDecoder
}