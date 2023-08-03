package com.my.nft_example.shared_data

import cats.data.NonEmptySet
import cats.effect.IO
import cats.implicits.{catsSyntaxApply, toFoldableOps, toTraverseOps}
import com.my.nft_example.shared_data.Data.{MintCollection, MintNFT, State, TransferCollection, TransferNFT}
import com.my.nft_example.shared_data.TypeValidators.{validateIfCollectionIsUnique, validateIfFromAddressIsTheCollectionOwner, validateIfFromAddressIsTheNFTOwner, validateIfNFTIdIsUnique, validateIfNFTUriIsUnique, validateIfNFTUriIsValid, validateIfProvidedCollectionExists, validateIfProvidedNFTExists, validateProvidedAddress}
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.security.SecurityProvider
import org.tessellation.security.signature.signature.SignatureProof

object Validations {
  def mintCollectionValidations(update: MintCollection, state: State): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateCollection = validateIfCollectionIsUnique(update, state)
    IO {
      validateCollection
    }
  }

  def mintNFTValidations(update: MintNFT, state: State): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateNFTUrlValid = validateIfNFTUriIsValid(update)
    val validateUniqueNFTURI = validateIfNFTUriIsUnique(update, state)
    val validateUniqueNFTId = validateIfNFTIdIsUnique(update, state)

    IO {
      validateNFTUrlValid.productR(validateUniqueNFTURI).productR(validateUniqueNFTId)
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

  def mintNFTValidationsWithSignature(update: MintNFT, proofs: NonEmptySet[SignatureProof], state: State)(implicit sp: SecurityProvider[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddress = proofs
      .map(_.id)
      .toList
      .traverse(_.toAddress[IO])
      .map(validateProvidedAddress(_, update.owner))

    val validations = mintNFTValidations(update, state)

    for {
      validatedAddress <- validateAddress
      validatedCollection <- validations
    } yield validatedAddress.productR(validatedCollection)
  }

  def transferCollectionValidationsWithSignature(update: TransferCollection, proofs: NonEmptySet[SignatureProof], state: State)(implicit sp: SecurityProvider[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddress = proofs
      .map(_.id)
      .toList
      .traverse(_.toAddress[IO])
      .map(validateProvidedAddress(_, update.fromAddress))

    val validations = transferCollectionValidations(update, state)

    for {
      validatedAddress <- validateAddress
      validatedCollection <- validations
    } yield validatedAddress.productR(validatedCollection)
  }

  def transferNFTValidationsWithSignature(update: TransferNFT, proofs: NonEmptySet[SignatureProof], state: State)(implicit sp: SecurityProvider[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddress = proofs
      .map(_.id)
      .toList
      .traverse(_.toAddress[IO])
      .map(validateProvidedAddress(_, update.fromAddress))

    val validations = transferNFTValidations(update, state)

    for {
      validatedAddress <- validateAddress
      validatedNFT <- validations
    } yield validatedAddress.productR(validatedNFT)
  }
}

