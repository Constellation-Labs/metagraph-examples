package com.my.data_l1

import cats.Monad
import cats.data.EitherT
import cats.implicits.{toBifunctorOps, toFunctorOps, toTraverseOps}

import org.tessellation.currency.dataApplication.{DataApplicationValidationError, L1NodeContext}
import org.tessellation.currency.schema.currency.CurrencyIncrementalSnapshot
import org.tessellation.json.JsonSerializer
import org.tessellation.schema.GlobalIncrementalSnapshot
import org.tessellation.security.Hashed

import com.my.shared_data.lib.JsonBinaryCodec
import com.my.shared_data.schema.OnChain

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

object DataL1NodeContext {

  object syntax {

    implicit class DataL1NodeContextOps[F[_]: Monad: JsonSerializer](ctx: L1NodeContext[F]) {

      def getLatestGlobalSnapshot: F[Either[DataApplicationValidationError, GlobalIncrementalSnapshot]] =
        EitherT
          .fromOptionF[F, DataApplicationValidationError, Hashed[GlobalIncrementalSnapshot]](
            ctx.getLastGlobalSnapshot,
            Errors.DataL1CtxCouldNotGetLatestGlobalSnapshot
          )
          .map(_.signed.value)
          .value

      def getOnChainState: F[Either[DataApplicationValidationError, OnChain]] =
        EitherT(getLatestCurrencySnapshot)
          .flatMapF { snapshot =>
            snapshot.dataApplication
              .toRight(Errors.DataL1CtxCouldNotGetLatestState: DataApplicationValidationError)
              .traverse { part =>
                JsonBinaryCodec[F]
                  .deserialize[OnChain](part.onChainState)
                  .map(_.leftMap { e =>
                    Errors.DataL1CtxFailedToDecodeState(e.getMessage): DataApplicationValidationError
                  })
              }
          }
          .value
          .map(_.flatten)

      def getLatestCurrencySnapshot: F[Either[DataApplicationValidationError, CurrencyIncrementalSnapshot]] =
        EitherT
          .fromOptionF[F, DataApplicationValidationError, Hashed[CurrencyIncrementalSnapshot]](
            ctx.getLastCurrencySnapshot,
            Errors.DataL1CtxCouldNotGetLatestCurrencySnapshot
          )
          .map(_.signed.value)
          .value
    }
  }

  object Errors {

    @derive(decoder, encoder)
    case object DataL1CtxCouldNotGetLatestCurrencySnapshot extends DataApplicationValidationError {
      val message = "Failed to retrieve latest currency snapshot from L1 node context!"
    }

    case object DataL1CtxCouldNotGetLatestState extends DataApplicationValidationError {
      val message = "Failed to retrieve latest state from L1 node context!"
    }

    @derive(decoder, encoder)
    case class DataL1CtxFailedToDecodeState(e: String) extends DataApplicationValidationError {
      val message = s"An error was encountered while decoding the state from L1 node context: $e"
    }

    @derive(decoder, encoder)
    case object DataL1CtxCouldNotGetLatestGlobalSnapshot extends DataApplicationValidationError {
      val message = "Failed to retrieve latest global snapshot from L1 node context!"
    }
  }
}
