package com.my.currency.shared_data

import cats.data.NonEmptyList
import cats.effect.IO
import com.my.currency.shared_data.Errors.{InvalidAddress, InvalidEndSnapshot, InvalidOption, NegativeTimestamp, PollAlreadyExists, PollDoesNotExists, RepeatedVote}
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, parser}
import io.circe.syntax.EncoderOps
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataApplicationValidationError, DataState, DataUpdate, L1NodeContext}
import org.tessellation.security.signature.Signed
import cats.syntax.all._
import com.my.currency.shared_data.Data.{NewPoll, PollState, PollUpdate, PollVote, State}
import com.my.currency.shared_data.Routes.{routeValidateData, routeValidateUpdate}
import com.my.currency.shared_data.TypeValidators.{validateIfNewPollExists, validateIfOptionExists, validateIfUserAlreadyVoted, validateIfVotePollExists, validateProvidedAddress, validateSnapshotNewPoll, validateTimestampPositive}
import com.my.currency.shared_data.Utils.{customStateDeserialization, customStateSerialization, customUpdateDeserialization, customUpdateSerialization}
import monocle.syntax.all._
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.address.Address
import org.tessellation.security.SecurityProvider
import org.tessellation.security.hash.Hash

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

  case object NegativeTimestamp extends DataApplicationValidationError {
    val message = "Timestamp should be greater than 0"
  }

  case object RepeatedVote extends DataApplicationValidationError {
    val message = "This user already voted!"
  }

  case object InvalidEndSnapshot extends DataApplicationValidationError {
    val message = "Provided end snapshot ordinal lower than current snapshot!"
  }
}

object TypeValidators {
  def validateIfNewPollExists(maybeState: Option[PollState]): DataApplicationValidationErrorOr[Unit] = {
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

  def validateTimestampPositive(update: PollUpdate): DataApplicationValidationErrorOr[Unit] = {
    if (update.timestamp > 0) {
      ().validNec
    } else {
      NegativeTimestamp.asInstanceOf[DataApplicationValidationError].invalidNec
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

  def validateSnapshotNewPoll(snapshotOrdinal: SnapshotOrdinal, update: NewPoll): DataApplicationValidationErrorOr[Unit] = {
    if (update.endSnapshotOrdinal < snapshotOrdinal.value.value) {
      InvalidEndSnapshot.asInstanceOf[DataApplicationValidationError].invalidNec
    } else {
      ().validNec
    }
  }
}

object Routes {
  def routeValidateUpdate(update: PollUpdate)(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    update match {
      case newPoll: NewPoll =>
        val validateNewPollSnapshot = context.getLastCurrencySnapshot.map(_.get.ordinal).map { lastSnapshotOrdinal =>
          validateSnapshotNewPoll(lastSnapshotOrdinal, newPoll)
        }

        val validateTimestamp = IO {
          validateTimestampPositive(newPoll)
        }

        for {
          validatedNewPollSnapshot <- validateNewPollSnapshot
          validatedTimestamp <- validateTimestamp
        } yield validatedNewPollSnapshot.productR(validatedTimestamp)

      case pollVote: PollVote =>
        val validateTimestamp = IO {
          validateTimestampPositive(pollVote)
        }
        validateTimestamp
    }
  }

  def routeValidateData(oldState: State, signedUpdate: Signed[PollUpdate])(implicit sp: SecurityProvider[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    signedUpdate.value match {
      case newPoll: NewPoll =>
        val validateAddress = signedUpdate.proofs
          .map(_.id)
          .toList
          .traverse(_.toAddress[IO])
          .map(validateProvidedAddress(_, newPoll.owner))

        val validatePoll = IO {
          val voteId = Hash.fromBytes(customUpdateSerialization(newPoll))
          validateIfNewPollExists(oldState.polls.get(voteId.toString))
        }

        for {
          validatedAddress <- validateAddress
          validatedPoll <- validatePoll
        } yield validatedAddress.productR(validatedPoll)

      case pollVote: PollVote =>
        val validateAddress = signedUpdate.proofs
          .map(_.id)
          .toList
          .traverse(_.toAddress[IO])
          .map(validateProvidedAddress(_, pollVote.address))


        val validatePoll = IO {
          validateIfVotePollExists(oldState.polls.get(pollVote.id))
        }

        val validateOption = IO {
          validateIfOptionExists(oldState.polls.get(pollVote.id), pollVote.option)
        }

        val validateRepeatedVote = IO {
          validateIfUserAlreadyVoted(oldState.polls.get(pollVote.id), pollVote.address)
        }

        for {
          validatedAddress <- validateAddress
          validatedPoll <- validatePoll
          validatedOption <- validateOption
          validatedRepeatedVote <- validateRepeatedVote
        } yield validatedAddress.productR(validatedPoll).productR(validatedOption).productR(validatedRepeatedVote)
    }
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
  sealed trait PollUpdate extends DataUpdate {
    val timestamp: Long
  }

  @derive(decoder, encoder)
  case class NewPoll(name: String, owner: Address, pollOptions: List[String], startSnapshotOrdinal: Long, endSnapshotOrdinal: Long, timestamp: Long) extends PollUpdate

  @derive(decoder, encoder)
  case class PollVote(id: String, address: Address, option: String, timestamp: Long) extends PollUpdate

  @derive(decoder, encoder)
  sealed trait PollState extends DataState {
    val name: String
    val owner: Address
    val pollOptions: Map[String, Long]
    val votes: Map[Address, Long]
    val startSnapshotOrdinal: Long
    val endSnapshotOrdinal: Long
    val timestamp: Long
  }

  @derive(decoder, encoder)
  case class Poll(name: String, owner: Address, pollOptions: Map[String, Long], votes: Map[Address, Long], startSnapshotOrdinal: Long, endSnapshotOrdinal: Long, timestamp: Long) extends PollState

  @derive(decoder, encoder)
  case class State(polls: Map[String, Poll]) extends DataState

  def validateUpdate(update: PollUpdate)(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    routeValidateUpdate(update)
  }

  def validateData(oldState: State, updates: NonEmptyList[Signed[PollUpdate]])(implicit sp: SecurityProvider[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    updates.traverse {
      update => routeValidateData(oldState, update)
    }.map(_.reduce)
  }

  def combine(oldState: State, updates: NonEmptyList[Signed[PollUpdate]]): IO[State] = IO {
    updates.foldLeft(oldState) { (acc, signedUpdate) => {
      val update = signedUpdate.value
      update match {
        case newPoll: NewPoll =>
          val pollOptions = newPoll.pollOptions.flatMap(option => Map(option -> 0L)).toMap
          val newState = Poll(newPoll.name, newPoll.owner, pollOptions, Map.empty, newPoll.startSnapshotOrdinal, newPoll.endSnapshotOrdinal, newPoll.timestamp)

          val voteId = Hash.fromBytes(customUpdateSerialization(newPoll))
          acc.focus(_.polls).modify(_.updated(voteId.toString, newState))
        case pollVote: PollVote =>
          val currentState = acc.polls(pollVote.id)
          val currentOptionNumber = currentState.pollOptions(pollVote.option)

          val newState = currentState
            .focus(_.pollOptions)
            .modify(_.updated(pollVote.option, currentOptionNumber + 1))
            .focus(_.votes)
            .modify(_.updated(pollVote.address, 1))

          acc.focus(_.polls).modify(_.updated(pollVote.id, newState))
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