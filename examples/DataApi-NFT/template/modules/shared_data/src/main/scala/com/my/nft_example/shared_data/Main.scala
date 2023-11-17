package com.my.nft_example.shared_data

import cats.data.NonEmptyList
import cats.effect.IO
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataState, L0NodeContext}
import org.tessellation.security.signature.Signed
import cats.syntax.all._
import com.my.nft_example.shared_data.combiners.Combiners.{combineMintCollection, combineMintNFT, combineTransferCollection, combineTransferNFT}
import com.my.nft_example.shared_data.types.Types.{MintCollection, MintNFT, NFTUpdate, NFTUpdatesCalculatedState, NFTUpdatesState, TransferCollection, TransferNFT}
import com.my.nft_example.shared_data.validations.Validations.{mintCollectionValidations, mintNFTValidations, mintNFTValidationsWithSignature, transferCollectionValidations, transferCollectionValidationsWithSignature, transferNFTValidations, transferNFTValidationsWithSignature}
import org.slf4j.LoggerFactory
import org.tessellation.security.SecurityProvider

object Main {
  private val logger = LoggerFactory.getLogger("Data")

  def validateUpdate(update: NFTUpdate): IO[DataApplicationValidationErrorOr[Unit]] = {
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

  def validateData(state: DataState[NFTUpdatesState, NFTUpdatesCalculatedState], updates: NonEmptyList[Signed[NFTUpdate]])(implicit context: L0NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    implicit val sp: SecurityProvider[IO] = context.securityProvider
    updates.traverse { signedUpdate =>
      signedUpdate.proofs
        .map(_.id)
        .toList
        .traverse(_.toAddress[IO])
        .flatMap { addresses =>
          signedUpdate.value match {
            case mintCollection: MintCollection =>
              mintCollectionValidations(mintCollection, state.some)
            case mintNFT: MintNFT =>
              mintNFTValidationsWithSignature(mintNFT, addresses, state)
            case transferCollection: TransferCollection =>
              transferCollectionValidationsWithSignature(transferCollection, addresses, state)
            case transferNFT: TransferNFT =>
              transferNFTValidationsWithSignature(transferNFT, addresses, state)
          }
        }
    }.map(_.reduce)
  }

  def combine(state: DataState[NFTUpdatesState, NFTUpdatesCalculatedState], updates: List[Signed[NFTUpdate]])(implicit context: L0NodeContext[IO]): IO[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]] = {
    val newStateIO = IO(DataState(NFTUpdatesState(List.empty), state.calculated))

    if (updates.isEmpty) {
      logger.info("Snapshot without any updates, updating the state to empty updates")
      return newStateIO
    }

    implicit val sp: SecurityProvider[IO] = context.securityProvider
    newStateIO.flatMap(newState => {
      updates.foldLeftM(newState) { (acc, signedUpdate) => {
        val update = signedUpdate.value
        update match {
          case mintCollection: MintCollection =>
            val collectionOwner = signedUpdate.proofs.map(_.id).toList.head.toAddress[IO]
            collectionOwner.map(address => combineMintCollection(mintCollection, acc, address))
          case mintNFT: MintNFT =>
            combineMintNFT(mintNFT, acc).pure[IO]
          case transferCollection: TransferCollection =>
            combineTransferCollection(transferCollection, acc).pure[IO]
          case transferNFT: TransferNFT =>
            combineTransferNFT(transferNFT, acc).pure[IO]
        }
      }
      }
    })
  }

}