package com.my.nft_example.shared_data.validations

import cats.effect.IO
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxApply, catsSyntaxOptionId, catsSyntaxValidatedIdBinCompat0}
import com.my.nft_example.shared_data.types.Types.{MintCollection, MintNFT, NFTUpdatesCalculatedState, NFTUpdatesState, TransferCollection, TransferNFT}
import com.my.nft_example.shared_data.validations.TypeValidators._
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.schema.address.Address

object Validations {
  def mintCollectionValidations(update: MintCollection, maybeState: Option[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]]): IO[DataApplicationValidationErrorOr[Unit]] = {
    maybeState match {
      case Some(state) =>
        val validateCollection = validateIfCollectionIsUnique(update, state)
        val validateCollectionName = validateStringMaxSize(update.name, 64, "name")
        IO {
          validateCollection.productR(validateCollectionName)
        }
      case None =>
        val validateCollectionName = validateStringMaxSize(update.name, 64, "name")
        IO {
          validateCollectionName
        }
    }
  }

  def mintNFTValidations(update: MintNFT, maybeState: Option[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]]): IO[DataApplicationValidationErrorOr[Unit]] = {
    maybeState match {
      case Some(state) =>
        val validateNFTUrlValid = validateIfNFTUriIsValid(update)
        val validateUniqueNFTURI = validateIfNFTUriIsUnique(update, state)
        val validateUniqueNFTId = validateIfNFTIdIsUnique(update, state)
        val validateNFTName = validateStringMaxSize(update.name, 64, "name")
        val validateNFTDescription = validateStringMaxSize(update.description, 64, "description")
        val validateNFTMetadata = validateMapMaxSize(update.metadata, 15, "metadata")

        IO {
          validateNFTUrlValid.productR(validateUniqueNFTURI).productR(validateUniqueNFTId).productR(validateNFTName).productR(validateNFTDescription).productR(validateNFTMetadata)
        }
      case None =>
        val validateNFTUrlValid = validateIfNFTUriIsValid(update)
        val validateNFTName = validateStringMaxSize(update.name, 64, "name")
        val validateNFTDescription = validateStringMaxSize(update.description, 64, "description")
        val validateNFTMetadata = validateMapMaxSize(update.metadata, 15, "metadata")

        IO {
          validateNFTUrlValid.productR(validateNFTName).productR(validateNFTDescription).productR(validateNFTMetadata)
        }
    }
  }

  def transferCollectionValidations(update: TransferCollection, maybeState: Option[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]]): IO[DataApplicationValidationErrorOr[Unit]] = {
    maybeState match {
      case Some(state) =>
        val validateProvidedCollection = validateIfProvidedCollectionExists(update, state)
        val validateFromAddress = validateIfFromAddressIsTheCollectionOwner(update, state)

        IO {
          validateProvidedCollection.productR(validateFromAddress)
        }
      case None => IO(().validNec)
    }

  }

  def transferNFTValidations(update: TransferNFT, maybeState: Option[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]]): IO[DataApplicationValidationErrorOr[Unit]] = {
    maybeState match {
      case Some(state) =>
        val validateProvidedNFT = validateIfProvidedNFTExists(update, state)
        val validateFromAddress = validateIfFromAddressIsTheNFTOwner(update, state)

        IO {
          validateProvidedNFT.productR(validateFromAddress)
        }
      case None => IO(().validNec)
    }
  }

  def mintNFTValidationsWithSignature(update: MintNFT, addresses: List[Address], state: DataState[NFTUpdatesState, NFTUpdatesCalculatedState]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddress = validateProvidedAddress(addresses, update.owner).pure[IO]
    val validations = mintNFTValidations(update, state.some)

    validateAddress.productR(validations)
  }

  def transferCollectionValidationsWithSignature(update: TransferCollection, addresses: List[Address], state: DataState[NFTUpdatesState, NFTUpdatesCalculatedState]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddress = validateProvidedAddress(addresses, update.fromAddress).pure[IO]
    val validations = transferCollectionValidations(update, state.some)

    validateAddress.productR(validations)
  }

  def transferNFTValidationsWithSignature(update: TransferNFT, addresses: List[Address], state: DataState[NFTUpdatesState, NFTUpdatesCalculatedState]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddress = validateProvidedAddress(addresses, update.fromAddress).pure[IO]
    val validations = transferNFTValidations(update, state.some)

    validateAddress.productR(validations)
  }
}

