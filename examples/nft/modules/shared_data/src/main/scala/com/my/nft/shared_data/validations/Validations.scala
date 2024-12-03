package com.my.nft.shared_data.validations

import cats.effect.Sync
import cats.syntax.all._

import org.tessellation.currency.dataApplication.DataState
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.schema.address.Address

import com.my.nft.shared_data.errors.Errors.valid
import com.my.nft.shared_data.schema._
import com.my.nft.shared_data.validations.TypeValidators._

object Validations {

  def mintCollectionValidations[F[_]: Sync](
    update:     MintCollection,
    maybeState: Option[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]]
  ): F[DataApplicationValidationErrorOr[Unit]] =
    maybeState match {
      case Some(state) =>
        validateIfCollectionIsUnique(update, state).map {
          _.productR(validateStringMaxSize(update.name, 64, "name"))
        }
      case None =>
        validateStringMaxSize(update.name, 64, "name").pure[F]

    }

  def mintNFTValidations(
    update:     MintNFT,
    maybeState: Option[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]]
  ): DataApplicationValidationErrorOr[Unit] =
    maybeState match {
      case Some(state) =>
        validateIfNFTUriIsValid(update)
          .productR(validateIfNFTUriIsUnique(update, state))
          .productR(validateIfNFTIdIsUnique(update, state))
          .productR(validateStringMaxSize(update.name, 64, "name"))
          .productR(validateStringMaxSize(update.description, 64, "description"))
          .productR(validateMapMaxSize(update.metadata, 15, "metadata"))
      case None =>
        validateIfNFTUriIsValid(update)
          .productR(validateStringMaxSize(update.name, 64, "name"))
          .productR(validateStringMaxSize(update.description, 64, "description"))
          .productR(validateMapMaxSize(update.metadata, 15, "metadata"))
    }

  def transferCollectionValidations(
    update:     TransferCollection,
    maybeState: Option[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]]
  ): DataApplicationValidationErrorOr[Unit] =
    maybeState match {
      case None => valid
      case Some(state) =>
        validateIfProvidedCollectionExists(update, state)
          .productR(validateIfFromAddressIsTheCollectionOwner(update, state))
    }

  def transferNFTValidations(
    update:     TransferNFT,
    maybeState: Option[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]]
  ): DataApplicationValidationErrorOr[Unit] =
    maybeState match {
      case None => valid
      case Some(state) =>
        validateIfProvidedNFTExists(update, state)
          .productR(validateIfFromAddressIsTheNFTOwner(update, state))
    }

  def mintNFTValidationsWithSignature(
    update: MintNFT,
    state:  DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] =
    mintNFTValidations(update, state.some)

  def transferCollectionValidationsWithSignature(
    update:    TransferCollection,
    addresses: List[Address],
    state:     DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] =
    transferCollectionValidations(update, state.some)
      .productR(validateProvidedAddress(addresses, update.fromAddress))

  def transferNFTValidationsWithSignature(
    update:    TransferNFT,
    addresses: List[Address],
    state:     DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] =
    transferNFTValidations(update, state.some)
      .productR(validateProvidedAddress(addresses, update.fromAddress))
}
