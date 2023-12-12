package com.my.currency.shared_data

import cats.data.NonEmptyList
import cats.effect.Async
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataState, L0NodeContext}
import org.tessellation.security.signature.Signed
import cats.syntax.all._
import com.my.currency.shared_data.types.Types.{UsageUpdate, UsageUpdateCalculatedState, UsageUpdateState}
import com.my.currency.shared_data.Utils.getAllAddressesFromProofs
import com.my.currency.shared_data.combiners.Combiners.combineUpdateUsage
import com.my.currency.shared_data.validations.Validations.{validateUsageUpdate, validateUsageUpdateSigned}
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.SecurityProvider
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object LifecycleSharedFunctions {
  private def logger[F[_] : Async]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromName[F]("LifecycleSharedFunctions")

  def validateUpdate[F[_] : Async](
    update: UsageUpdate
  ): F[DataApplicationValidationErrorOr[Unit]] = Async[F].delay {
    validateUsageUpdate(update, none)
  }

  def validateData[F[_] : Async : SecurityProvider](
    oldState: DataState[UsageUpdateState, UsageUpdateCalculatedState],
    updates : NonEmptyList[Signed[UsageUpdate]]
  ): F[DataApplicationValidationErrorOr[Unit]] =
    updates.traverse { update =>
      val addressesF = getAllAddressesFromProofs(update.proofs)
      addressesF
        .flatMap(addresses => Async[F].delay(validateUsageUpdateSigned(update, oldState.calculated, addresses)))
    }.map(_.reduce)

  def combine[F[_] : Async : SecurityProvider](
    oldState: DataState[UsageUpdateState, UsageUpdateCalculatedState],
    updates : List[Signed[UsageUpdate]]
  )(implicit context: L0NodeContext[F]): F[DataState[UsageUpdateState, UsageUpdateCalculatedState]] = {
    val newState = DataState(
      UsageUpdateState(List.empty),
      UsageUpdateCalculatedState(oldState.calculated.devices)
    )

    val lastSnapshotOrdinal: F[SnapshotOrdinal] = context.getLastCurrencySnapshot.flatMap {
      case Some(value) => value.ordinal.pure[F]
      case None =>
        val message = "Could not get the ordinal from currency snapshot. lastCurrencySnapshot not found"
        logger.error(message) >> new Exception(message).raiseError[F, SnapshotOrdinal]
    }

    if (updates.isEmpty) {
      logger.info("Snapshot without any check-ins, updating the state to empty updates").as(newState)
    } else {
      updates.foldLeftM(newState) { (acc, signedUpdate) =>
        lastSnapshotOrdinal.flatMap(ordinal =>
          Async[F].delay(combineUpdateUsage(signedUpdate, acc, ordinal))
        )
      }
    }
  }
}