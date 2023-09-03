package com.my.currency.shared_data

import cats.data.NonEmptyList
import cats.effect.IO
import com.my.currency.shared_data.Errors.{CouldNotGetLatestCurrencySnapshot, CouldNotGetLatestState}
import io.circe.{Decoder, Encoder}
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataApplicationValidationError, L0NodeContext, L1NodeContext}
import org.tessellation.security.signature.Signed
import cats.syntax.all._
import com.my.currency.shared_data.Combiners.combineUpdateUsage
import com.my.currency.shared_data.Types.{UsageState, UsageUpdate}
import com.my.currency.shared_data.Utils.customStateDeserialization
import com.my.currency.shared_data.Validations.{validateUsageUpdate, validateUsageUpdateSigned}
import org.tessellation.security.SecurityProvider

object Data {

  def validateUpdate(update: UsageUpdate)(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val lastCurrencySnapshot = context.getLastCurrencySnapshot
    lastCurrencySnapshot.map(_.get.data).flatMap {
      case None => CouldNotGetLatestCurrencySnapshot.asInstanceOf[DataApplicationValidationError].invalidNec.pure[IO]
      case Some(state) =>
        val currentState = customStateDeserialization(state)
        currentState match {
          case Left(_) => IO.pure(CouldNotGetLatestState.asInstanceOf[DataApplicationValidationError].invalidNec)
          case Right(state) =>
            validateUsageUpdate(update, state)
        }
    }

  }

  def validateData(oldState: UsageState, updates: NonEmptyList[Signed[UsageUpdate]])(implicit sp: SecurityProvider[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    updates.traverse { update =>
      validateUsageUpdateSigned(update, oldState)
    }.map(_.reduce)
  }

  def combine(oldState: UsageState, updates: NonEmptyList[Signed[UsageUpdate]])(implicit context: L0NodeContext[IO]): IO[UsageState] = {
    val newState = oldState.copy(transactions = Map.empty)
    context.getLastCurrencySnapshot.map(_.get.ordinal).map { lastSnapshotOrdinal =>
      val currentSnapshotOrdinal = lastSnapshotOrdinal.value.value + 1
      updates.foldLeft(newState) { (acc, signedUpdate) => {
        combineUpdateUsage(signedUpdate, acc, currentSnapshotOrdinal)
      }
      }
    }
  }

  def serializeState(state: UsageState): IO[Array[Byte]] = IO {
    Utils.customStateSerialization(state)
  }

  def deserializeState(bytes: Array[Byte]): IO[Either[Throwable, UsageState]] = IO {
    Utils.customStateDeserialization(bytes)
  }

  def serializeUpdate(update: UsageUpdate): IO[Array[Byte]] = IO {
    Utils.customUpdateSerialization(update)
  }

  def deserializeUpdate(bytes: Array[Byte]): IO[Either[Throwable, UsageUpdate]] = IO {
    Utils.customUpdateDeserialization(bytes)
  }

  def dataEncoder: Encoder[UsageUpdate] = Encoder[UsageUpdate]

  def dataDecoder: Decoder[UsageUpdate] = Decoder[UsageUpdate]
}