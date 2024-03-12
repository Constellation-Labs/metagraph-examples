package com.my.nft.shared_data

import cats.data.NonEmptyList
import cats.effect.Async
import cats.syntax.all._
import com.my.nft.shared_data.Utils._
import com.my.nft.shared_data.combiners.Combiners._
import com.my.nft.shared_data.types.Types._
import com.my.nft.shared_data.validations.Validations._
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataState, L0NodeContext}
import org.tessellation.security.SecurityProvider
import org.tessellation.security.signature.Signed
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object LifecycleSharedFunctions {
  private def logger[F[_] : Async]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromName[F]("ClusterApi")

  def validateUpdate[F[_] : Async](
    update: NFTUpdate
  ): F[DataApplicationValidationErrorOr[Unit]] = Async[F].delay {
    update match {
      case mintCollection: MintCollection =>
        mintCollectionValidations(mintCollection, None)
      case mintNFT: MintNFT =>
        mintNFTValidations(mintNFT, None)
      case transferCollection: TransferCollection =>
        transferCollectionValidations(transferCollection, None)
      case transferNFT: TransferNFT =>
        transferNFTValidations(transferNFT, None)
    }
  }

  def validateData[F[_] : Async](
    state  : DataState[NFTUpdatesState, NFTUpdatesCalculatedState],
    updates: NonEmptyList[Signed[NFTUpdate]]
  )(implicit context: L0NodeContext[F]): F[DataApplicationValidationErrorOr[Unit]] = {
    implicit val sp: SecurityProvider[F] = context.securityProvider
    updates.traverse { signedUpdate =>
      getAllAddressesFromProofs(signedUpdate.proofs)
        .flatMap { addresses =>
          Async[F].delay {
            signedUpdate.value match {
              case mintCollection: MintCollection =>
                mintCollectionValidations(mintCollection, state.some)
              case mintNFT: MintNFT =>
                mintNFTValidationsWithSignature(mintNFT, state)
              case transferCollection: TransferCollection =>
                transferCollectionValidationsWithSignature(transferCollection, addresses, state)
              case transferNFT: TransferNFT =>
                transferNFTValidationsWithSignature(transferNFT, addresses, state)
            }
          }
        }
    }.map(_.reduce)
  }

  def combine[F[_] : Async](
    state  : DataState[NFTUpdatesState, NFTUpdatesCalculatedState],
    updates: List[Signed[NFTUpdate]]
  )(implicit context: L0NodeContext[F]): F[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]] = {
    val newStateF = DataState(NFTUpdatesState(List.empty), state.calculated).pure[F]

    if (updates.isEmpty) {
      logger.info("Snapshot without any updates, updating the state to empty updates") >> newStateF
    } else {
      implicit val sp: SecurityProvider[F] = context.securityProvider
      newStateF.flatMap(newState => {
        updates.foldLeftM(newState) { (acc, signedUpdate) => {
          signedUpdate.value match {
            case mintCollection: MintCollection =>
              getFirstAddressFromProofs(signedUpdate.proofs)
                .map(combineMintCollection(mintCollection, acc, _))
            case mintNFT: MintNFT =>
              Async[F].delay(combineMintNFT(mintNFT, acc))
            case transferCollection: TransferCollection =>
              Async[F].delay(combineTransferCollection(transferCollection, acc))
            case transferNFT: TransferNFT =>
              Async[F].delay(combineTransferNFT(transferNFT, acc))
          }
        }
        }
      })
    }
  }
}