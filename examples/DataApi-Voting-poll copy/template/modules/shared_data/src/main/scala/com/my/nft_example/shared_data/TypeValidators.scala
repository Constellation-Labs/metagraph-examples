package com.my.nft_example.shared_data

import cats.implicits.catsSyntaxValidatedIdBinCompat0
import com.my.nft_example.shared_data.Data.{MintCollection, MintNFT, State, TransferCollection, TransferNFT}
import com.my.nft_example.shared_data.Errors.{CollectionDoesNotBelongsToProvidedAddress, CollectionNotExists, DuplicatedCollection, InvalidAddress, InvalidNFTUri, NFTAlreadyExists, NFTDoesNotBelongsToProvidedAddress, NFTNotExists, NFTUriAlreadyExists}
import com.my.nft_example.shared_data.Utils.{customUpdateSerialization, isValidURL}
import org.tessellation.currency.dataApplication.DataApplicationValidationError
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.schema.address.Address
import org.tessellation.security.hash.Hash

object TypeValidators {
  def validateIfCollectionIsUnique(update: MintCollection, state: State): DataApplicationValidationErrorOr[Unit] = {
    val collectionId = Hash.fromBytes(customUpdateSerialization(update)).toString
    state.collections.get(collectionId) match {
      case Some(_) => DuplicatedCollection.asInstanceOf[DataApplicationValidationError].invalidNec
      case None => ().validNec
    }
  }

  def validateIfNFTUriIsValid(update: MintNFT): DataApplicationValidationErrorOr[Unit] = {
    if (isValidURL(update.uri)) {
      ().validNec
    } else {
      InvalidNFTUri.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }

  def validateIfNFTUriIsUnique(update: MintNFT, state: State): DataApplicationValidationErrorOr[Unit] = {
    state.collections.get(update.collectionId) match {
      case Some(value) =>
        val uris = value.nfts.map { case (_, value) => value.uri }.toList
        if (uris.contains(update.uri)) {
          NFTUriAlreadyExists.asInstanceOf[DataApplicationValidationError].invalidNec
        } else {
          ().validNec
        }
      case None => CollectionNotExists.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }

  def validateIfNFTIdIsUnique(update: MintNFT, state: State): DataApplicationValidationErrorOr[Unit] = {
    state.collections.get(update.collectionId) match {
      case Some(value) =>
        val uris = value.nfts.map { case (_, value) => value.id }.toList
        if (uris.contains(update.nftId)) {
          NFTAlreadyExists.asInstanceOf[DataApplicationValidationError].invalidNec
        } else {
          ().validNec
        }
      case None => CollectionNotExists.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }

  def validateIfProvidedNFTExists(update: TransferNFT, state: State): DataApplicationValidationErrorOr[Unit] = {
    state.collections.get(update.collectionId) match {
      case Some(value) =>
        value.nfts.get(update.nftId) match {
          case Some(_) => ().validNec
          case None => NFTNotExists.asInstanceOf[DataApplicationValidationError].invalidNec
        }
      case None => CollectionNotExists.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }

  def validateIfFromAddressIsTheNFTOwner(update: TransferNFT, state: State): DataApplicationValidationErrorOr[Unit] = {
    state.collections.get(update.collectionId) match {
      case Some(value) =>
        value.nfts.get(update.nftId) match {
          case Some(nft) =>
            if (nft.owner == update.fromAddress) {
              ().validNec
            } else {
              NFTDoesNotBelongsToProvidedAddress.asInstanceOf[DataApplicationValidationError].invalidNec
            }
          case None => NFTNotExists.asInstanceOf[DataApplicationValidationError].invalidNec
        }
      case None => CollectionNotExists.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }

  def validateIfFromAddressIsTheCollectionOwner(update: TransferCollection, state: State): DataApplicationValidationErrorOr[Unit] = {
    state.collections.get(update.collectionId) match {
      case Some(collection) =>
        if (collection.owner == update.fromAddress) {
          ().validNec
        } else {
          CollectionDoesNotBelongsToProvidedAddress.asInstanceOf[DataApplicationValidationError].invalidNec
        }
      case None => CollectionNotExists.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }

  def validateIfProvidedCollectionExists(update: TransferCollection, state: State): DataApplicationValidationErrorOr[Unit] = {
    state.collections.get(update.collectionId) match {
      case Some(_) =>
        ().validNec
      case None => CollectionNotExists.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }

  def validateProvidedAddress(proofAddresses: List[Address], address: Address): DataApplicationValidationErrorOr[Unit] = {
    if (proofAddresses.contains(address)) {
      ().validNec
    } else {
      InvalidAddress.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }
}

