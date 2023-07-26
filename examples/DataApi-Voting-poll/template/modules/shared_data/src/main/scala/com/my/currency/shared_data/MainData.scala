package com.my.currency.shared_data

import cats.data.NonEmptyList
import cats.effect.IO
import com.my.currency.shared_data.Errors.{CouldNotGetLatestCurrencySnapshot, CouldNotGetLatestState}
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataApplicationValidationError, DataState, DataUpdate, L0NodeContext, L1NodeContext}
import org.tessellation.security.signature.Signed
import cats.syntax.all._
import com.my.currency.shared_data.Combiners.{combineCreatePoll, combineVoteInPoll}
import com.my.currency.shared_data.Validations.{createPollValidations, createPollValidationsWithSignature, voteInPollValidations, voteInPollValidationsWithSignature}
import com.my.currency.shared_data.Utils.{customStateDeserialization, customStateSerialization, customUpdateDeserialization, customUpdateSerialization}
import org.tessellation.schema.address.Address

object MainData {
  @derive(decoder, encoder)
  sealed trait PollUpdate extends DataUpdate

  @derive(decoder, encoder)
  case class CreatePoll(name: String, owner: Address, pollOptions: List[String], startSnapshotOrdinal: Long, endSnapshotOrdinal: Long) extends PollUpdate

  @derive(decoder, encoder)
  case class VoteInPoll(pollId: String, address: Address, option: String) extends PollUpdate

  @derive(decoder, encoder)
  sealed trait PollState extends DataState {
    val name: String
    val owner: Address
    val pollOptions: Map[String, Long]
    val usersVotes: Map[Address, Map[String, Long]]
    val startSnapshotOrdinal: Long
    val endSnapshotOrdinal: Long
  }

  @derive(decoder, encoder)
  case class Poll(id: String, name: String, owner: Address, pollOptions: Map[String, Long], usersVotes: Map[Address, Map[String, Long]], startSnapshotOrdinal: Long, endSnapshotOrdinal: Long) extends PollState

  @derive(decoder, encoder)
  case class State(polls: Map[String, Poll]) extends DataState

  def validateUpdate(update: PollUpdate)(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val lastCurrencySnapshot = context.getLastCurrencySnapshot
    lastCurrencySnapshot.map(_.get.data).flatMap {
      case Some(state) =>
        val currentState = customStateDeserialization(state)
        currentState match {
          case Left(_) => IO.pure(CouldNotGetLatestState.asInstanceOf[DataApplicationValidationError].invalidNec)
          case Right(state) =>
            lastCurrencySnapshot.map(_.get.ordinal).flatMap { lastSnapshotOrdinal =>
              update match {
                case poll: CreatePoll =>
                  createPollValidations(poll, state, Some(lastSnapshotOrdinal))
                case voteInPoll: VoteInPoll =>
                  context.getLastCurrencySnapshotCombined.map(_.get._2).flatMap { snapshotInfo =>
                    voteInPollValidations(voteInPoll, state, Some(lastSnapshotOrdinal), snapshotInfo)
                  }
              }
            }
        }
      case None => IO.pure(CouldNotGetLatestCurrencySnapshot.asInstanceOf[DataApplicationValidationError].invalidNec)
    }
  }

  def validateData(oldState: State, updates: NonEmptyList[Signed[PollUpdate]])(implicit context: L0NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    implicit val sp = context.securityProvider
    updates.traverse { signedUpdate =>
      signedUpdate.value match {
        case poll: CreatePoll =>
          createPollValidationsWithSignature(poll, signedUpdate.proofs, oldState)
        case voteInPoll: VoteInPoll =>
          context.getLastCurrencySnapshotCombined.map(_.get._2).flatMap { snapshotInfo =>
            voteInPollValidationsWithSignature(voteInPoll, signedUpdate.proofs, oldState, snapshotInfo)
          }
      }
    }.map(_.reduce)
  }

  def combine(oldState: State, updates: NonEmptyList[Signed[PollUpdate]])(implicit context: L0NodeContext[IO]): IO[State] = {
    context.getLastCurrencySnapshotCombined.map(_.get._2).map { snapshotInfo =>
      updates.foldLeft(oldState) { (acc, signedUpdate) => {
        val update = signedUpdate.value
        update match {
          case poll: CreatePoll =>
            combineCreatePoll(poll, acc)
          case voteInPoll: VoteInPoll =>
            combineVoteInPoll(voteInPoll, acc, snapshotInfo)
        }
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