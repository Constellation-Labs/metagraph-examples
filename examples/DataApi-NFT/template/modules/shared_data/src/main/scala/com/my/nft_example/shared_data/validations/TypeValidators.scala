package com.my.nft_example.shared_data.validations

import cats.implicits.catsSyntaxValidatedIdBinCompat0
import com.my.nft_example.shared_data.errors.Errors._
import com.my.nft_example.shared_data.Utils.isValidURL
import com.my.nft_example.shared_data.serializers.Serializers
import com.my.nft_example.shared_data.types.Types.{MintCollection, MintNFT, NFTUpdatesCalculatedState, NFTUpdatesState, TransferCollection, TransferNFT}
import org.tessellation.currency.dataApplication.{DataApplicationValidationError, DataState}
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.schema.address.Address
import org.tessellation.security.hash.Hash

object TypeValidators {
  def validateIfCollectionIsUnique(update: MintCollection, state: DataState[NFTUpdatesState, NFTUpdatesCalculatedState]): DataApplicationValidationErrorOr[Unit] = {
    val collectionId = Hash.fromBytes(Serializers.serializeUpdate(update)).toString
    state.calculated.collections.get(collectionId) match {
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

  def validateIfNFTUriIsUnique(update: MintNFT, state: DataState[NFTUpdatesState, NFTUpdatesCalculatedState]): DataApplicationValidationErrorOr[Unit] = {
    state.calculated.collections.get(update.collectionId) match {
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

  def validateIfNFTIdIsUnique(update: MintNFT, state: DataState[NFTUpdatesState, NFTUpdatesCalculatedState]): DataApplicationValidationErrorOr[Unit] = {
    state.calculated.collections.get(update.collectionId) match {
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

  def validateIfProvidedNFTExists(update: TransferNFT, state: DataState[NFTUpdatesState, NFTUpdatesCalculatedState]): DataApplicationValidationErrorOr[Unit] = {
    state.calculated.collections.get(update.collectionId) match {
      case Some(value) =>
        value.nfts.get(update.nftId) match {
          case Some(_) => ().validNec
          case None => NFTNotExists.asInstanceOf[DataApplicationValidationError].invalidNec
        }
      case None => CollectionNotExists.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }

  def validateIfFromAddressIsTheNFTOwner(update: TransferNFT, state: DataState[NFTUpdatesState, NFTUpdatesCalculatedState]): DataApplicationValidationErrorOr[Unit] = {
    state.calculated.collections.get(update.collectionId).flatMap { collection =>
      collection.nfts.get(update.nftId).map { nft => {
        if (nft.owner == update.fromAddress) {
          ().validNec
        } else {
          NFTDoesNotBelongsToProvidedAddress.asInstanceOf[DataApplicationValidationError].invalidNec
        }
      }
      }
    }.getOrElse(CollectionNotExists.asInstanceOf[DataApplicationValidationError].invalidNec)
  }

  def validateIfFromAddressIsTheCollectionOwner(update: TransferCollection, state: DataState[NFTUpdatesState, NFTUpdatesCalculatedState]): DataApplicationValidationErrorOr[Unit] = {
    state.calculated.collections.get(update.collectionId) match {
      case Some(collection) =>
        if (collection.owner == update.fromAddress) {
          ().validNec
        } else {
          CollectionDoesNotBelongsToProvidedAddress.asInstanceOf[DataApplicationValidationError].invalidNec
        }
      case None => CollectionNotExists.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }

  def validateIfProvidedCollectionExists(update: TransferCollection, state: DataState[NFTUpdatesState, NFTUpdatesCalculatedState]): DataApplicationValidationErrorOr[Unit] = {
    state.calculated.collections.get(update.collectionId) match {
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

  def validateStringMaxSize(value: String, maxSize: Long, fieldName: String): DataApplicationValidationErrorOr[Unit] = {
    if (value.length > maxSize) {
      InvalidFieldSize(fieldName, maxSize).asInstanceOf[DataApplicationValidationError].invalidNec
    } else {
      ().validNec
    }
  }

  def validateMapMaxSize(value: Map[String, String], maxSize: Long, fieldName: String): DataApplicationValidationErrorOr[Unit] = {
    if (value.size > maxSize) {
      InvalidFieldSize(fieldName, maxSize).asInstanceOf[DataApplicationValidationError].invalidNec
    } else {
      ().validNec
    }
  }
}

