package com.my.currency.shared_data

import cats.conversions.all.autoWidenFunctor
import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits.catsSyntaxValidatedIdBinCompat0
import com.my.currency.shared_data.Utils.getLastMetagraphIncrementalSnapshotInfo
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataApplicationValidationError, DataState, L0NodeContext, L1NodeContext}
import org.tessellation.security.signature.Signed
import com.my.currency.shared_data.combiners.Combiners.{combineCreatePoll, combineVoteInPoll}
import com.my.currency.shared_data.errors.Errors.CouldNotGetLatestCurrencySnapshot
import com.my.currency.shared_data.validations.Validations.{createPollValidations, createPollValidationsWithSignature, voteInPollValidations, voteInPollValidationsWithSignature}
import com.my.currency.shared_data.types.Types.{CreatePoll, PollUpdate, VoteCalculatedState, VoteInPoll, VoteStateOnChain}
import org.tessellation.security.SecurityProvider

object Main {

  def validateUpdate(update: PollUpdate)(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val lastCurrencySnapshot = context.getLastCurrencySnapshot

    lastCurrencySnapshot.map(_.get.ordinal).flatMap { lastSnapshotOrdinal =>
      update match {
        case poll: CreatePoll =>
          createPollValidations(poll, None, Some(lastSnapshotOrdinal))
        case voteInPoll: VoteInPoll =>
          getLastMetagraphIncrementalSnapshotInfo(Right(context)) match {
            case Some(snapshotInfo) => voteInPollValidations(voteInPoll, None, Some(lastSnapshotOrdinal), snapshotInfo)
            case None => IO(CouldNotGetLatestCurrencySnapshot.asInstanceOf[DataApplicationValidationError].invalidNec)
          }
      }
    }
  }

  def validateData(state: DataState[VoteStateOnChain, VoteCalculatedState], updates: NonEmptyList[Signed[PollUpdate]])(implicit context: L0NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    implicit val sp: SecurityProvider[IO] = context.securityProvider
    updates.traverse { signedUpdate =>
      signedUpdate.value match {
        case poll: CreatePoll =>
          createPollValidationsWithSignature(poll, signedUpdate.proofs, state)
        case voteInPoll: VoteInPoll =>
          getLastMetagraphIncrementalSnapshotInfo(Left(context)) match {
            case Some(snapshotInfo) => voteInPollValidationsWithSignature(voteInPoll, signedUpdate.proofs, state, snapshotInfo)
            case None => IO(CouldNotGetLatestCurrencySnapshot.asInstanceOf[DataApplicationValidationError].invalidNec)
          }
      }
    }.map(_.reduce)
  }

  def combine(state: DataState[VoteStateOnChain, VoteCalculatedState], updates: List[Signed[PollUpdate]])(implicit context: L0NodeContext[IO]): IO[DataState[VoteStateOnChain, VoteCalculatedState]] = {
    if (updates.isEmpty) {
      return IO(DataState(VoteStateOnChain(List.empty), state.calculated))
    }
    getLastMetagraphIncrementalSnapshotInfo(Left(context)) match {
      case None => println("Could not get lastMetagraphIncrementalSnapshotInfo, keeping current state")
        IO(state)
      case Some(snapshotInfo) =>
        val updatedState = updates.foldLeft(state) { (acc, signedUpdate) => {
          val update = signedUpdate.value
          update match {
            case poll: CreatePoll =>
              combineCreatePoll(poll, acc)
            case voteInPoll: VoteInPoll =>
              combineVoteInPoll(voteInPoll, acc, snapshotInfo)
          }
        }
        }
        IO(updatedState)
    }
  }

  def dataEncoder: Encoder[PollUpdate] = deriveEncoder

  def dataDecoder: Decoder[PollUpdate] = deriveDecoder
}