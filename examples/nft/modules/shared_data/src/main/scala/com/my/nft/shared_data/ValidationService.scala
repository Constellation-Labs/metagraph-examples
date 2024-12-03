package com.my.nft.shared_data

import cats.effect.Async
import cats.syntax.flatMap._
import cats.syntax.option._

import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataState, L0NodeContext, L1NodeContext}
import org.tessellation.security.SecurityProvider
import org.tessellation.security.signature.Signed

import com.my.nft.shared_data.Utils.getAllAddressesFromProofs
import com.my.nft.shared_data.schema._
import com.my.nft.shared_data.validations.Validations._

import io.constellationnetwork.metagraph_sdk.lifecycle.ValidationService

object ValidationService {

  def make[F[_]: Async: SecurityProvider]: ValidationService[F, NFTUpdate, NFTUpdatesState, NFTUpdatesCalculatedState] =
    new ValidationService[F, NFTUpdate, NFTUpdatesState, NFTUpdatesCalculatedState] {

      override def validateUpdate(
        update: NFTUpdate
      )(implicit ctx: L1NodeContext[F]): F[DataApplicationValidationErrorOr[Unit]] =
        update match {
          case mintCollection: MintCollection =>
            mintCollectionValidations(mintCollection, None)
          case mintNFT: MintNFT =>
            Async[F].delay(mintNFTValidations(mintNFT, None))
          case transferCollection: TransferCollection =>
            Async[F].delay(transferCollectionValidations(transferCollection, None))
          case transferNFT: TransferNFT =>
            Async[F].delay(transferNFTValidations(transferNFT, None))
        }

      override def validateSignedUpdate(
        current:      DataState[NFTUpdatesState, NFTUpdatesCalculatedState],
        signedUpdate: Signed[NFTUpdate]
      )(implicit context: L0NodeContext[F]): F[DataApplicationValidationErrorOr[Unit]] =
        getAllAddressesFromProofs(signedUpdate.proofs)
          .flatMap { addresses =>
            signedUpdate.value match {
              case mintCollection: MintCollection =>
                mintCollectionValidations(mintCollection, current.some)
              case mintNFT: MintNFT =>
                Async[F].delay(mintNFTValidationsWithSignature(mintNFT, current))
              case transferCollection: TransferCollection =>
                Async[F].delay(transferCollectionValidationsWithSignature(transferCollection, addresses, current))
              case transferNFT: TransferNFT =>
                Async[F].delay(transferNFTValidationsWithSignature(transferNFT, addresses, current))
            }
          }
    }
}
