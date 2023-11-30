package com.my.nft_example.shared_data.validations

import cats.effect.Async
import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.apply.catsSyntaxApply
import cats.syntax.option.catsSyntaxOptionId
import com.my.nft_example.shared_data.errors.Errors.valid
import com.my.nft_example.shared_data.types.Types._
import com.my.nft_example.shared_data.validations.TypeValidators._
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.schema.address.Address

object Validations {
  def mintCollectionValidations[F[_] : Async](
    update    : MintCollection,
    maybeState: Option[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]]
  ): F[DataApplicationValidationErrorOr[Unit]] =
    maybeState match {
      case Some(state) =>
        Async[F].delay {
          val validateCollection = validateIfCollectionIsUnique(update, state)
          val validateCollectionName = validateStringMaxSize(update.name, 64, "name")
          validateCollection.productR(validateCollectionName)
        }
      case None =>
        Async[F].delay {
          validateStringMaxSize(update.name, 64, "name")
        }
    }

  def mintNFTValidations[F[_] : Async](
    update    : MintNFT,
    maybeState: Option[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]]
  ): F[DataApplicationValidationErrorOr[Unit]] =
    maybeState match {
      case Some(state) =>
        Async[F].delay {
          val validateNFTUrlValid = validateIfNFTUriIsValid(update)
          val validateUniqueNFTURI = validateIfNFTUriIsUnique(update, state)
          val validateUniqueNFTId = validateIfNFTIdIsUnique(update, state)
          val validateNFTName = validateStringMaxSize(update.name, 64, "name")
          val validateNFTDescription = validateStringMaxSize(update.description, 64, "description")
          val validateNFTMetadata = validateMapMaxSize(update.metadata, 15, "metadata")

          validateNFTUrlValid
            .productR(validateUniqueNFTURI)
            .productR(validateUniqueNFTId)
            .productR(validateNFTName)
            .productR(validateNFTDescription)
            .productR(validateNFTMetadata)
        }
      case None =>
        Async[F].delay {
          val validateNFTUrlValid = validateIfNFTUriIsValid(update)
          val validateNFTName = validateStringMaxSize(update.name, 64, "name")
          val validateNFTDescription = validateStringMaxSize(update.description, 64, "description")
          val validateNFTMetadata = validateMapMaxSize(update.metadata, 15, "metadata")

          validateNFTUrlValid
            .productR(validateNFTName)
            .productR(validateNFTDescription)
            .productR(validateNFTMetadata)
        }
    }

  def transferCollectionValidations[F[_] : Async](
    update    : TransferCollection,
    maybeState: Option[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]]
  ): F[DataApplicationValidationErrorOr[Unit]] =
    maybeState match {
      case None => valid.pure[F]
      case Some(state) =>
        Async[F].delay {
          val validateProvidedCollection = validateIfProvidedCollectionExists(update, state)
          val validateFromAddress = validateIfFromAddressIsTheCollectionOwner(update, state)

          validateProvidedCollection.productR(validateFromAddress)
        }
    }


  def transferNFTValidations[F[_] : Async](
    update    : TransferNFT,
    maybeState: Option[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]]
  ): F[DataApplicationValidationErrorOr[Unit]] =
    maybeState match {
      case None => valid.pure[F]
      case Some(state) =>
        Async[F].delay {
          val validateProvidedNFT = validateIfProvidedNFTExists(update, state)
          val validateFromAddress = validateIfFromAddressIsTheNFTOwner(update, state)

          validateProvidedNFT.productR(validateFromAddress)
        }
    }

  def mintNFTValidationsWithSignature[F[_] : Async](
    update   : MintNFT,
    addresses: List[Address],
    state    : DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): F[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddress = Async[F].delay(validateProvidedAddress(addresses, update.owner))
    val validations = mintNFTValidations(update, state.some)

    validateAddress.productR(validations)
  }

  def transferCollectionValidationsWithSignature[F[_] : Async](
    update   : TransferCollection,
    addresses: List[Address],
    state    : DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): F[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddress = Async[F].delay(validateProvidedAddress(addresses, update.fromAddress))
    val validations = transferCollectionValidations(update, state.some)

    validateAddress.productR(validations)
  }

  def transferNFTValidationsWithSignature[F[_] : Async](
    update   : TransferNFT,
    addresses: List[Address],
    state    : DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): F[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddress = Async[F].delay(validateProvidedAddress(addresses, update.fromAddress))
    val validations = transferNFTValidations(update, state.some)

    validateAddress.productR(validations)
  }
}

