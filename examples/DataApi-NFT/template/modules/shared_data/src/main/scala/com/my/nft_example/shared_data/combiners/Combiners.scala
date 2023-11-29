package com.my.nft_example.shared_data.combiners

import com.my.nft_example.shared_data.serializers.Serializers
import com.my.nft_example.shared_data.types.Types.{Collection, MintCollection, MintNFT, NFT, NFTUpdatesCalculatedState, NFTUpdatesState, TransferCollection, TransferNFT}
import monocle.Monocle.toAppliedFocusOps
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.schema.address.Address
import org.tessellation.security.hash.Hash

object Combiners {
  def combineMintCollection(
    update         : MintCollection,
    state          : DataState[NFTUpdatesState, NFTUpdatesCalculatedState],
    collectionOwner: Address
  ): DataState[NFTUpdatesState, NFTUpdatesCalculatedState] = {
    val collectionId = Hash.fromBytes(Serializers.serializeUpdate(update)).toString
    val nowInTime = System.currentTimeMillis()
    val newState = Collection(collectionId, collectionOwner, update.name, nowInTime, Map.empty)

    val newUpdatesList = state.onChain.updates :+ update
    val newCalculatedState = state.calculated.focus(_.collections).modify(_.updated(collectionId, newState))

    DataState(NFTUpdatesState(newUpdatesList), newCalculatedState)
  }

  def combineMintNFT(
    update: MintNFT,
    state : DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): DataState[NFTUpdatesState, NFTUpdatesCalculatedState] = {
    val nowInTime = System.currentTimeMillis()
    val newNFT = NFT(update.nftId, update.collectionId, update.owner, update.uri, update.name, update.description, nowInTime, update.metadata)

    val collection = state.calculated.collections(update.collectionId)
    val collectionNFTs = collection.nfts + (update.nftId -> newNFT)
    val newState = Collection(collection.id, collection.owner, collection.name, collection.creationDateTimestamp, collectionNFTs)

    val newUpdatesList = state.onChain.updates :+ update
    val newCalculatedState = state.calculated.focus(_.collections).modify(_.updated(update.collectionId, newState))

    DataState(NFTUpdatesState(newUpdatesList), newCalculatedState)
  }

  def combineTransferCollection(
    update: TransferCollection,
    state : DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): DataState[NFTUpdatesState, NFTUpdatesCalculatedState] =
    state.calculated.collections.get(update.collectionId) match {
      case None => state
      case Some(collection) =>
        val newState = collection.copy(owner = update.toAddress)

        val newUpdatesList = state.onChain.updates :+ update
        val newCalculatedState = state.calculated.focus(_.collections).modify(_.updated(update.collectionId, newState))

        DataState(NFTUpdatesState(newUpdatesList), newCalculatedState)
    }

  def combineTransferNFT(
    update: TransferNFT,
    state : DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): DataState[NFTUpdatesState, NFTUpdatesCalculatedState] =
    state.calculated.collections.get(update.collectionId) match {
      case None => state
      case Some(collection) =>
        collection.nfts.get(update.nftId) match {
          case Some(nft) =>
            val updatedNFT = NFT(nft.id, nft.collectionId, update.toAddress, nft.uri, nft.name, nft.description, nft.creationDateTimestamp, nft.metadata)
            val collectionNFTs = collection.nfts + (nft.id -> updatedNFT)
            val newState = Collection(collection.id, collection.owner, collection.name, collection.creationDateTimestamp, collectionNFTs)

            val newUpdatesList = state.onChain.updates :+ update
            val newCalculatedState = state.calculated.focus(_.collections).modify(_.updated(update.collectionId, newState))

            DataState(NFTUpdatesState(newUpdatesList), newCalculatedState)
          case None => state
        }
    }
}