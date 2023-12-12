package com.my.currency.l0.custom_routes

import cats.effect.Async
import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.applicativeError.catsSyntaxApplicativeErrorId
import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps
import com.my.currency.shared_data.calculated_state.CalculatedStateService
import com.my.currency.shared_data.deserializers.Deserializers
import com.my.currency.shared_data.types.Types.{AddressTransactionsWithLastRef, TxnRef, UpdateUsageTransaction}
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import eu.timepit.refined.auto._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.CORS
import org.tessellation.currency.dataApplication.L0NodeContext
import org.tessellation.ext.http4s.AddressVar
import org.tessellation.routes.internal.{InternalUrlPrefix, PublicRoutes}
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.address.Address

case class CustomRoutes[F[_] : Async](
  calculatedStateService: CalculatedStateService[F],
  context               : L0NodeContext[F]
) extends Http4sDsl[F] with PublicRoutes[F] {

  @derive(encoder, decoder)
  case class TransactionResponse(
    transactionType   : String,
    energyUpdateAmount: Long,
    waterUpdateAmount : Long,
    txnSnapshotOrdinal: SnapshotOrdinal,
    txnHash           : String,
    lastRef           : TxnRef
  )

  private object TransactionResponse {
    def make(updateUsageTransaction: UpdateUsageTransaction, snapshotOrdinal: SnapshotOrdinal, txnRef: String): TransactionResponse = {
      TransactionResponse(
        updateUsageTransaction.transactionType,
        updateUsageTransaction.energyUpdateAmount,
        updateUsageTransaction.waterUpdateAmount,
        snapshotOrdinal,
        txnRef,
        TxnRef(updateUsageTransaction.lastTxnOrdinal, updateUsageTransaction.lastTxnHash)
      )
    }
  }

  private def getAddressTransactionsFromState(
    ordinal: SnapshotOrdinal,
    address: Address
  ): F[AddressTransactionsWithLastRef] = {
    context.getCurrencySnapshot(ordinal).flatMap {
      case None => new Exception(s"Could not fetch snapshot: ${ordinal.value.value}").raiseError[F, AddressTransactionsWithLastRef]
      case Some(value) =>
        value.dataApplication
          .fold(
            AddressTransactionsWithLastRef(TxnRef.empty, List.empty[UpdateUsageTransaction]).pure
          )(value =>
            Deserializers.deserializeState(value.onChainState) match {
              case Left(_) => new Exception(s"Could not deserialize state of snapshot: ${ordinal.value.value}").raiseError[F, AddressTransactionsWithLastRef]
              case Right(value) =>
                Async[F].delay {
                  val updateUsageTransactions =  value.updates.filter { t => t.owner == address }.sortBy(_.lastTxnOrdinal)(Ordering[SnapshotOrdinal].reverse)
                  val referenceTransaction = updateUsageTransactions.head

                  AddressTransactionsWithLastRef(
                    TxnRef(referenceTransaction.lastTxnOrdinal, referenceTransaction.lastTxnHash),
                    updateUsageTransactions
                  )
                }
            }
          )
    }
  }

  private def traverseSnapshotsWithTransactions(
    address        : Address,
    startingOrdinal: SnapshotOrdinal,
    txnHash        : String,
    transactions   : List[TransactionResponse]
  ): F[List[TransactionResponse]] = {
    getAddressTransactionsFromState(startingOrdinal, address).flatMap { addressTransactionsWithLastRef =>
      if (addressTransactionsWithLastRef.txnRef.txnSnapshotOrdinal == SnapshotOrdinal.MinValue) {
        Async[F].delay {
          transactions ++ addressTransactionsWithLastRef.txns.map(TransactionResponse.make(_, startingOrdinal, txnHash))
        }
      } else {
        traverseSnapshotsWithTransactions(
          address,
          addressTransactionsWithLastRef.txnRef.txnSnapshotOrdinal,
          addressTransactionsWithLastRef.txnRef.txnHash,
          transactions
        )
          .map(_ ++ addressTransactionsWithLastRef.txns.map(TransactionResponse.make(_, startingOrdinal, txnHash)))
      }
    }
  }

  private def getAllAddressTransactions(
    address: Address
  ): F[List[TransactionResponse]] = {
    calculatedStateService.getCalculatedState.flatMap { calculatedState =>
      calculatedState.state.devices
        .get(address)
        .fold(List.empty[TransactionResponse].pure) { deviceCalculatedState =>
          val txnSnapshotOrdinal: SnapshotOrdinal = deviceCalculatedState.currentTxnRef.txnSnapshotOrdinal
          val txnHash: String = deviceCalculatedState.currentTxnRef.txnHash
          traverseSnapshotsWithTransactions(address, txnSnapshotOrdinal, txnHash, List.empty[TransactionResponse])
        }
    }
  }

  private def getAllDevices: F[Response[F]] = {
    calculatedStateService.getCalculatedState
      .flatMap(value => Ok(value.state.devices))
  }

  private def getDeviceByAddress(
    address: Address
  ): F[Response[F]] =
    calculatedStateService.getCalculatedState
      .flatMap { value =>
        value.state.devices.get(address)
          .map(addressInfo => Ok(addressInfo.usages))
          .getOrElse(NotFound())
      }

  private def getDeviceTransactions(
    address: Address
  ): F[Response[F]] = {
    Ok(
      getAllAddressTransactions(address)
        .map(_.sortBy(_.txnSnapshotOrdinal)(Ordering[SnapshotOrdinal].reverse))
    )
  }

  private val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "addresses" => getAllDevices
    case GET -> Root / "addresses" / AddressVar(address) => getDeviceByAddress(address)
    case GET -> Root / "addresses" / AddressVar(address) / "transactions" => getDeviceTransactions(address)
  }

  val public: HttpRoutes[F] =
    CORS
      .policy
      .withAllowCredentials(false)
      .httpRoutes(routes)

  override protected def prefixPath: InternalUrlPrefix = "/"
}