package com.my.metagraph_l0

import cats.data.NonEmptyList
import cats.effect.{Async, Sync}
import cats.implicits.toFlatMapOps
import cats.syntax.all._

import scala.collection.immutable.SortedSet

import com.my.shared_data.lib.{LatestUpdateValidator, SignedUpdateReducer}
import com.my.shared_data.schema.Updates.TodoUpdate
import com.my.shared_data.schema.{CalculatedState, OnChain}

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.{Decoder, Encoder}
import io.constellationnetwork.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import io.constellationnetwork.currency.dataApplication.{
  DataApplicationValidationError,
  DataState,
  DataUpdate,
  L0NodeContext
}
import io.constellationnetwork.metagraph_sdk.syntax.all.{CurrencyIncrementalSnapshotOps, L0ContextOps}
import io.constellationnetwork.schema.SnapshotOrdinal
import io.constellationnetwork.security.SecurityProvider
import io.constellationnetwork.security.signature.Signed

object ML0NodeContext {

  object syntax {

    implicit class ML0NodeContextOps[F[_]: Sync](ctx: L0NodeContext[F]) {

      def countUpdatesInSnapshotAt(
        ordinal: SnapshotOrdinal
      )(implicit ue: Encoder[DataUpdate], de: Decoder[DataUpdate]): F[Either[DataApplicationValidationError, Long]] =
        ctx.getCurrencySnapshotAt(ordinal).flatMap(_.traverse(_.countUpdates))

    }

    implicit class dataStateOps[F[_]: Async: SecurityProvider](
      dataState: DataState[OnChain, CalculatedState]
    )(implicit ctx: L0NodeContext[F]) {

      def insert(batch: SortedSet[Signed[TodoUpdate]])(implicit
        ev: SignedUpdateReducer[F, TodoUpdate, DataState[OnChain, CalculatedState]]
      ): F[DataState[OnChain, CalculatedState]] =
        ev.foldLeft(dataState, batch)

      def verify(batch: NonEmptyList[Signed[TodoUpdate]])(implicit
        ev: LatestUpdateValidator[F, Signed[TodoUpdate], DataState[OnChain, CalculatedState]]
      ): F[DataApplicationValidationErrorOr[Unit]] =
        ev.checkAll(dataState, batch)
    }
  }

  object Errors {

    @derive(decoder, encoder)
    case object ML0CtxCouldNotGetLatestCurrencySnapshot extends DataApplicationValidationError {
      val message = "Failed to retrieve latest currency snapshot from L0 node context!"
    }

    case object ML0CtxCouldNotGetLatestState extends DataApplicationValidationError {
      val message = "Failed to retrieve latest state from L0 node context!"
    }

    @derive(decoder, encoder)
    case object ML0CtxFailedToDecodeState extends DataApplicationValidationError {
      val message = "An error was encountered while decoding the state from L0 node context"
    }

    @derive(decoder, encoder)
    case object ML0CtxCouldNotGetLatestGlobalSnapshot extends DataApplicationValidationError {
      val message = "Failed to retrieve latest global snapshot from L0 node context!"
    }
  }
}
