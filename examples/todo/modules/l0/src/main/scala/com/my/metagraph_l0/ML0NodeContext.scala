package com.my.metagraph_l0

import cats.data.{EitherT, NonEmptyList}
import cats.effect.{Async, Sync}
import cats.implicits.{toFlatMapOps, toFunctorOps}
import cats.syntax.all._

import scala.collection.immutable.SortedSet

import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataApplicationValidationError, DataState, L0NodeContext}
import org.tessellation.currency.schema.currency.CurrencyIncrementalSnapshot
import org.tessellation.json.JsonSerializer
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.signature.Signed
import org.tessellation.security.{Hashed, SecurityProvider}

import com.my.shared_data.lib.syntax.CurrencyIncrementalSnapshotOps
import com.my.shared_data.lib.{JsonBinaryCodec, LatestUpdateValidator, SignedUpdateReducer}
import com.my.shared_data.schema.Updates.TodoUpdate
import com.my.shared_data.schema.{CalculatedState, OnChain}

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

object ML0NodeContext {

  object syntax {

    implicit class ML0NodeContextOps[F[_]: Sync: JsonSerializer](ctx: L0NodeContext[F]) {

      def getOnChainState: F[Either[DataApplicationValidationError, OnChain]] =
        EitherT(getLatestCurrencySnapshot)
          .flatMapF { snapshot =>
            snapshot.dataApplication
              .toRight(Errors.ML0CtxCouldNotGetLatestState: DataApplicationValidationError)
              .traverse { part =>
                JsonBinaryCodec[F]
                  .deserialize[OnChain](part.onChainState)
                  .map(_.leftMap(_ => Errors.ML0CtxFailedToDecodeState: DataApplicationValidationError))
              }
          }
          .value
          .map(_.flatten)

      def getLatestCurrencySnapshot: F[Either[DataApplicationValidationError, CurrencyIncrementalSnapshot]] =
        EitherT
          .fromOptionF[F, DataApplicationValidationError, Hashed[CurrencyIncrementalSnapshot]](
            ctx.getLastCurrencySnapshot,
            Errors.ML0CtxCouldNotGetLatestCurrencySnapshot
          )
          .map(_.signed.value)
          .value

      def countUpdatesInSnapshotAt(ordinal: SnapshotOrdinal): F[Either[DataApplicationValidationError, Long]] =
        getCurrencySnapshotAt(ordinal).flatMap(_.traverse(_.countUpdates))

      def getCurrencySnapshotAt(
        ordinal: SnapshotOrdinal
      ): F[Either[DataApplicationValidationError, CurrencyIncrementalSnapshot]] =
        EitherT
          .fromOptionF[F, DataApplicationValidationError, Hashed[CurrencyIncrementalSnapshot]](
            ctx.getCurrencySnapshot(ordinal),
            Errors.ML0CtxCouldNotGetLatestGlobalSnapshot
          )
          .map(_.signed.value)
          .value
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
