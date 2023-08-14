package com.my.nft_example.shared_data

import com.my.nft_example.shared_data.Data.{Collection, MintCollection, MintNFT, NFT, State, TransferCollection, TransferNFT}
import com.my.nft_example.shared_data.Utils.customUpdateSerialization
import monocle.Monocle.toAppliedFocusOps
import org.tessellation.schema.address.Address
import org.tessellation.security.hash.Hash

object Combiners {
  def combineMintCollection(update: MintCollection, acc: State, collectionOwner: Address): State = {
    val collectionId = Hash.fromBytes(customUpdateSerialization(update)).toString
    val nowInTime = System.currentTimeMillis()
    val newState = Collection(collectionId, collectionOwner, update.name, nowInTime, Map.empty)

    acc.focus(_.collections).modify(_.updated(collectionId, newState))
  }

  def combineMintNFT(update: MintNFT, acc: State): State = {
    val nowInTime = System.currentTimeMillis()
    val newNFT = NFT(update.nftId, update.collectionId, update.owner, update.uri, update.name, update.description, nowInTime, update.metadata)

    val collection = acc.collections(update.collectionId)
    val collectionNFTs = collection.nfts + (update.nftId -> newNFT)
    val newState = Collection(collection.id, collection.owner, collection.name, collection.creationDateTimestamp, collectionNFTs)

    acc.focus(_.collections).modify(_.updated(update.collectionId, newState))
  }

  def combineTransferCollection(update: TransferCollection, acc: State): State = {
    acc.collections.get(update.collectionId) match {
      case Some(collection) =>
        val newState = collection.asInstanceOf[Collection].copy(owner = update.toAddress)
        acc.focus(_.collections).modify(_.updated(update.collectionId, newState))
      case None => acc
    }
  }

  def combineTransferNFT(update: TransferNFT, acc: State): State = {
    acc.collections.get(update.collectionId) match {
      case Some(collection) =>
        collection.nfts.get(update.nftId) match {
          case Some(nft) =>
            val updatedNFT = NFT(nft.id, nft.collectionId, update.toAddress, nft.uri, nft.name, nft.description, nft.creationDateTimestamp, nft.metadata)
            val collectionNFTs = collection.nfts + (nft.id -> updatedNFT)

            val newState = Collection(collection.id, collection.owner, collection.name, collection.creationDateTimestamp, collectionNFTs.toMap)
            acc.focus(_.collections).modify(_.updated(update.collectionId, newState))
          case None => acc
        }
      case None => acc
    }
  }
}