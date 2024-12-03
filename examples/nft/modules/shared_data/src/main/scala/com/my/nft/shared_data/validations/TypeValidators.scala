package com.my.nft.shared_data.validations

import cats.effect.Sync
import cats.syntax.functor._

import org.tessellation.currency.dataApplication.DataState
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.schema.address.Address

import com.my.nft.shared_data.Utils.isValidURL
import com.my.nft.shared_data.errors.Errors._
import com.my.nft.shared_data.schema._

import io.constellationnetwork.metagraph_sdk.std.JsonBinaryHasher.FromJsonBinaryCodec

object TypeValidators {

  private def getCollectionById(
    collectionId: String,
    state:        DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): Option[NFTCollection] =
    state.calculated.collections
      .get(collectionId)

  def validateIfCollectionIsUnique[F[_]: Sync](
    update: MintCollection,
    state:  DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): F[DataApplicationValidationErrorOr[Unit]] =
    (update: NFTUpdate).hash.map { collectionId =>
      DuplicatedCollection.whenA(state.calculated.collections.contains(collectionId.toString))
    }

  def validateIfNFTUriIsValid(
    update: MintNFT
  ): DataApplicationValidationErrorOr[Unit] =
    InvalidNFTUri.unlessA(isValidURL(update.uri))

  def validateIfNFTUriIsUnique(
    update: MintNFT,
    state:  DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] =
    getCollectionById(update.collectionId, state)
      .map { value =>
        val uris = value.nfts.map { case (_, value) => value.uri }.toList
        NFTUriAlreadyExists.whenA(uris.contains(update.uri))
      }
      .getOrElse(CollectionNotExists.invalid)

  def validateIfNFTIdIsUnique(
    update: MintNFT,
    state:  DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] =
    getCollectionById(update.collectionId, state)
      .map { value =>
        val ids = value.nfts.map { case (_, value) => value.id }.toList
        NFTAlreadyExists.whenA(ids.contains(update.nftId))
      }
      .getOrElse(CollectionNotExists.invalid)

  def validateIfProvidedNFTExists(
    update: TransferNFT,
    state:  DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] =
    getCollectionById(update.collectionId, state)
      .map { value =>
        NFTNotExists.unlessA(value.nfts.contains(update.nftId))
      }
      .getOrElse(CollectionNotExists.invalid)

  def validateIfFromAddressIsTheNFTOwner(
    update: TransferNFT,
    state:  DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] =
    getCollectionById(update.collectionId, state)
      .map { value =>
        NFTDoesNotBelongsToProvidedAddress.unlessA(value.nfts.get(update.nftId).exists(_.owner == update.fromAddress))
      }
      .getOrElse(CollectionNotExists.invalid)

  def validateIfFromAddressIsTheCollectionOwner(
    update: TransferCollection,
    state:  DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] =
    getCollectionById(update.collectionId, state)
      .map { value =>
        CollectionDoesNotBelongsToProvidedAddress.unlessA(value.owner == update.fromAddress)
      }
      .getOrElse(CollectionNotExists.invalid)

  def validateIfProvidedCollectionExists(
    update: TransferCollection,
    state:  DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] =
    CollectionNotExists.unlessA(state.calculated.collections.contains(update.collectionId))

  def validateProvidedAddress(
    proofAddresses: List[Address],
    address:        Address
  ): DataApplicationValidationErrorOr[Unit] =
    InvalidAddress.unlessA(proofAddresses.contains(address))

  def validateStringMaxSize(
    value:     String,
    maxSize:   Long,
    fieldName: String
  ): DataApplicationValidationErrorOr[Unit] =
    InvalidFieldSize(fieldName, maxSize).whenA(value.length > maxSize)

  def validateMapMaxSize(
    value:     Map[String, String],
    maxSize:   Long,
    fieldName: String
  ): DataApplicationValidationErrorOr[Unit] =
    InvalidFieldSize(fieldName, maxSize).whenA(value.size > maxSize)
}
