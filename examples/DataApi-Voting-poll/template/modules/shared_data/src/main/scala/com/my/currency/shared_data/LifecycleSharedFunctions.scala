package com.my.currency.shared_data

import cats.data.NonEmptyList
import cats.effect.Async
import cats.syntax.applicative._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import com.my.currency.shared_data.Utils.getLastMetagraphIncrementalSnapshotInfo
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataState, L0NodeContext, L1NodeContext}
import org.tessellation.security.signature.Signed
import com.my.currency.shared_data.combiners.Combiners.{combineCreatePoll, combineVoteInPoll}
import com.my.currency.shared_data.errors.Errors.CouldNotGetLatestCurrencySnapshot
import com.my.currency.shared_data.errors.Errors.DataApplicationValidationTypeOps
import com.my.currency.shared_data.validations.Validations.{createPollValidations, createPollValidationsWithSignature, voteInPollValidations, voteInPollValidationsWithSignature}
import com.my.currency.shared_data.types.Types.{CreatePoll, PollUpdate, VoteCalculatedState, VoteInPoll, VoteStateOnChain}
import org.slf4j.LoggerFactory
import org.tessellation.security.SecurityProvider

object LifecycleSharedFunctions {

  private val logger = LoggerFactory.getLogger("Data")

  def validateUpdate[F[_] : Async](update: PollUpdate)(implicit context: L1NodeContext[F]): F[DataApplicationValidationErrorOr[Unit]] = {
    context
      .getLastCurrencySnapshot
      .map(_.get.ordinal)
      .flatMap { lastSnapshotOrdinal =>
        update match {
          case poll: CreatePoll => createPollValidations(poll, none, lastSnapshotOrdinal.some)
          case voteInPoll: VoteInPoll =>
            getLastMetagraphIncrementalSnapshotInfo(context.asRight[L0NodeContext[F]])
              .flatMap {
                case Some(snapshotInfo) => voteInPollValidations(voteInPoll, none, lastSnapshotOrdinal.some, snapshotInfo)
                case None => CouldNotGetLatestCurrencySnapshot.invalid.pure[F]
              }
        }
      }
  }

  def validateData[F[_] : Async](state: DataState[VoteStateOnChain, VoteCalculatedState], updates: NonEmptyList[Signed[PollUpdate]])(implicit context: L0NodeContext[F]): F[DataApplicationValidationErrorOr[Unit]] = {
    implicit val sp: SecurityProvider[F] = context.securityProvider
    updates.traverse { signedUpdate =>
      signedUpdate.value match {
        case poll: CreatePoll =>
          createPollValidationsWithSignature(poll, signedUpdate.proofs, state)
        case voteInPoll: VoteInPoll =>
          getLastMetagraphIncrementalSnapshotInfo(context.asLeft[L1NodeContext[F]]).flatMap {
            case Some(snapshotInfo) => voteInPollValidationsWithSignature(voteInPoll, signedUpdate.proofs, state, snapshotInfo)
            case None => CouldNotGetLatestCurrencySnapshot.invalid.pure[F]
          }
      }
    }.map(_.reduce)
  }

  def combine[F[_] : Async](state: DataState[VoteStateOnChain, VoteCalculatedState], updates: List[Signed[PollUpdate]])(implicit context: L0NodeContext[F]): F[DataState[VoteStateOnChain, VoteCalculatedState]] = {
    val newStateF = DataState(VoteStateOnChain(List.empty), state.calculated).pure

    if (updates.isEmpty) {
      logger.info("Snapshot without any update, updating the state to empty updates")
      newStateF
    } else {
      getLastMetagraphIncrementalSnapshotInfo(Left(context)).flatMap {
        case None =>
          logger.info("Could not get lastMetagraphIncrementalSnapshotInfo, keeping current state")
          state.pure
        case Some(snapshotInfo) =>
          newStateF.flatMap(newState => {
            val updatedState = updates.foldLeft(newState) { (acc, signedUpdate) => {
              val update = signedUpdate.value
              update match {
                case poll: CreatePoll =>
                  combineCreatePoll(poll, acc)
                case voteInPoll: VoteInPoll =>
                  combineVoteInPoll(voteInPoll, acc, snapshotInfo)
              }
            }
            }
            updatedState.pure
          })
      }
    }
  }
}