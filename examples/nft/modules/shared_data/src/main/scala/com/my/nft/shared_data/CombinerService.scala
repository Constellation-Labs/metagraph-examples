package com.my.nft.shared_data

import cats.effect.{Async, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._

import org.tessellation.currency.dataApplication.{DataState, L0NodeContext}
import org.tessellation.schema.address.Address
import org.tessellation.security.SecurityProvider
import org.tessellation.security.signature.Signed

import com.my.nft.shared_data.Utils.getFirstAddressFromProofs
import com.my.nft.shared_data.schema._

import io.constellationnetwork.metagraph_sdk.lifecycle.CombinerService
import io.constellationnetwork.metagraph_sdk.std.JsonBinaryHasher.FromJsonBinaryCodec
import monocle.Monocle.toAppliedFocusOps

object CombinerService {

  def make[F[_]: Async: SecurityProvider]: CombinerService[F, NFTUpdate, NFTUpdatesState, NFTUpdatesCalculatedState] =
    new CombinerService[F, NFTUpdate, NFTUpdatesState, NFTUpdatesCalculatedState] {

      override def insert(previous: DataState[NFTUpdatesState, NFTUpdatesCalculatedState], update: Signed[NFTUpdate])(
        implicit ctx: L0NodeContext[F]
      ): F[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]] =
        update.value match {
          case mintCollection: MintCollection =>
            getFirstAddressFromProofs(update.proofs)
              .flatMap(combineMintCollection(mintCollection, previous, _))
          case mintNFT: MintNFT =>
            Async[F].delay(combineMintNFT(mintNFT, previous))
          case transferCollection: TransferCollection =>
            Async[F].delay(combineTransferCollection(transferCollection, previous))
          case transferNFT: TransferNFT =>
            Async[F].delay(combineTransferNFT(transferNFT, previous))
        }
    }

  private def combineMintCollection[F[_]: Sync](
    update:          MintCollection,
    state:           DataState[NFTUpdatesState, NFTUpdatesCalculatedState],
    collectionOwner: Address
  ): F[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]] = (update: NFTUpdate).hash.map { updateHash =>
    val collectionId = updateHash.toString
    val nowInTime = System.currentTimeMillis()
    val newState = NFTCollection(collectionId, collectionOwner, update.name, nowInTime, Map.empty)

    val newUpdatesList = state.onChain.updates :+ update
    val newCalculatedState = state.calculated.focus(_.collections).modify(_.updated(collectionId, newState))

    DataState(NFTUpdatesState(newUpdatesList), newCalculatedState)
  }

  private def combineMintNFT(
    update: MintNFT,
    state:  DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): DataState[NFTUpdatesState, NFTUpdatesCalculatedState] = {
    val nowInTime = System.currentTimeMillis()
    val newNFT = NFT(
      update.nftId,
      update.collectionId,
      update.owner,
      update.uri,
      update.name,
      update.description,
      nowInTime,
      update.metadata
    )

    val collection = state.calculated.collections(update.collectionId)
    val collectionNFTs = collection.nfts + (update.nftId -> newNFT)
    val newState =
      NFTCollection(collection.id, collection.owner, collection.name, collection.creationDateTimestamp, collectionNFTs)

    val newUpdatesList = state.onChain.updates :+ update
    val newCalculatedState = state.calculated.focus(_.collections).modify(_.updated(update.collectionId, newState))

    DataState(NFTUpdatesState(newUpdatesList), newCalculatedState)
  }

  private def combineTransferCollection(
    update: TransferCollection,
    state:  DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): DataState[NFTUpdatesState, NFTUpdatesCalculatedState] =
    state.calculated.collections
      .get(update.collectionId)
      .fold(state) { collection =>
        val newState = collection.copy(owner = update.toAddress)

        val newUpdatesList = state.onChain.updates :+ update
        val newCalculatedState = state.calculated.focus(_.collections).modify(_.updated(update.collectionId, newState))

        DataState(NFTUpdatesState(newUpdatesList), newCalculatedState)
      }

  private def combineTransferNFT(
    update: TransferNFT,
    state:  DataState[NFTUpdatesState, NFTUpdatesCalculatedState]
  ): DataState[NFTUpdatesState, NFTUpdatesCalculatedState] =
    state.calculated.collections
      .get(update.collectionId)
      .fold(state) { collection =>
        collection.nfts
          .get(update.nftId)
          .fold(state) { nft =>
            val updatedNFT = NFT(
              nft.id,
              nft.collectionId,
              update.toAddress,
              nft.uri,
              nft.name,
              nft.description,
              nft.creationDateTimestamp,
              nft.metadata
            )
            val collectionNFTs = collection.nfts + (nft.id -> updatedNFT)
            val newState = NFTCollection(
              collection.id,
              collection.owner,
              collection.name,
              collection.creationDateTimestamp,
              collectionNFTs
            )

            val newUpdatesList = state.onChain.updates :+ update
            val newCalculatedState = state.calculated
              .focus(_.collections)
              .modify(_.updated(update.collectionId, newState))

            DataState(NFTUpdatesState(newUpdatesList), newCalculatedState)
          }
      }
}
