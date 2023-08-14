package com.my.nft_example.shared_data

import cats.effect.IO
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxApply}
import com.my.nft_example.shared_data.Data.{MintCollection, MintNFT, State, TransferCollection, TransferNFT}
import com.my.nft_example.shared_data.TypeValidators.{validateIfCollectionIsUnique, validateIfFromAddressIsTheCollectionOwner, validateIfFromAddressIsTheNFTOwner, validateIfNFTIdIsUnique, validateIfNFTUriIsUnique, validateIfNFTUriIsValid, validateIfProvidedCollectionExists, validateIfProvidedNFTExists, validateMapMaxSize, validateProvidedAddress, validateStringMaxSize}
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.schema.address.Address

object Validations {
  def mintCollectionValidations(update: MintCollection, state: State): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateCollection = validateIfCollectionIsUnique(update, state)
    val validateCollectionName = validateStringMaxSize(update.name, 64, "name")

    IO {
      validateCollection.productR(validateCollectionName)
    }
  }

  def mintNFTValidations(update: MintNFT, state: State): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateNFTUrlValid = validateIfNFTUriIsValid(update)
    val validateUniqueNFTURI = validateIfNFTUriIsUnique(update, state)
    val validateUniqueNFTId = validateIfNFTIdIsUnique(update, state)
    val validateNFTName = validateStringMaxSize(update.name, 64, "name")
    val validateNFTDescription = validateStringMaxSize(update.description, 64, "description")
    val validateNFTMetadata = validateMapMaxSize(update.metadata, 15, "metadata")

    IO {
      validateNFTUrlValid.productR(validateUniqueNFTURI).productR(validateUniqueNFTId).productR(validateNFTName).productR(validateNFTDescription).productR(validateNFTMetadata)
    }
  }

  def transferCollectionValidations(update: TransferCollection, state: State): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateProvidedCollection = validateIfProvidedCollectionExists(update, state)
    val validateFromAddress = validateIfFromAddressIsTheCollectionOwner(update, state)

    IO {
      validateProvidedCollection.productR(validateFromAddress)
    }
  }

  def transferNFTValidations(update: TransferNFT, state: State): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateProvidedNFT = validateIfProvidedNFTExists(update, state)
    val validateFromAddress = validateIfFromAddressIsTheNFTOwner(update, state)

    IO {
      validateProvidedNFT.productR(validateFromAddress)
    }
  }

  def mintNFTValidationsWithSignature(update: MintNFT, addresses: List[Address], state: State): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddress = validateProvidedAddress(addresses, update.owner).pure[IO]
    val validations = mintNFTValidations(update, state)

    validateAddress.productR(validations)
  }

  def transferCollectionValidationsWithSignature(update: TransferCollection, addresses: List[Address], state: State): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddress = validateProvidedAddress(addresses, update.fromAddress).pure[IO]
    val validations = transferCollectionValidations(update, state)

    validateAddress.productR(validations)
  }

  def transferNFTValidationsWithSignature(update: TransferNFT, addresses: List[Address], state: State): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddress = validateProvidedAddress(addresses, update.fromAddress).pure[IO]
    val validations = transferNFTValidations(update, state)

    validateAddress.productR(validations)
  }
}

