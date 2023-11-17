package com.my.nft_example.l0.custom_routes

import cats.effect.IO
import com.my.nft_example.shared_data.calculated_state.CalculatedState.getCalculatedState
import com.my.nft_example.shared_data.types.Types.{Collection, NFT, NFTUpdatesCalculatedState}
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.tessellation.schema.address.Address

object CustomRoutes {

  @derive(decoder, encoder)
  case class CollectionResponse(id: String, owner: Address, name: String, creationDateTimestamp: Long, numberOfNFTs: Long)

  @derive(decoder, encoder)
  case class NFTResponse(id: Long, collectionId: String, owner: Address, uri: String, name: String, description: String, creationDateTimestamp: Long, metadata: Map[String, String])

  private def formatToCollectionResponse(collection: Collection): CollectionResponse = {
    CollectionResponse(collection.id, collection.owner, collection.name, collection.creationDateTimestamp, collection.nfts.size.toLong)
  }

  private def formatToNFTResponse(nft: NFT): NFTResponse = {
    NFTResponse(nft.id, nft.collectionId, nft.owner, nft.uri, nft.name, nft.description, nft.creationDateTimestamp, nft.metadata)
  }

  private def getState: IO[NFTUpdatesCalculatedState] = {
    val calculatedState = getCalculatedState
    calculatedState.map(_._2)
  }

  def getAllCollections: IO[Response[IO]] = {
    getState.flatMap { state =>
      val allCollectionsResponse = state.collections.map { case (_, collection) => formatToCollectionResponse(collection) }.toList
      Ok(allCollectionsResponse)
    }
  }

  def getCollectionById(collectionId: String): IO[Response[IO]] = {
    getState.flatMap { state =>
      state.collections.get(collectionId).map { value =>
        Ok(formatToCollectionResponse(value))
      }.getOrElse(NotFound())
    }
  }

  def getCollectionNFTs(collectionId: String): IO[Response[IO]] = {
    getState.flatMap { state =>
      state.collections.get(collectionId).map { value =>
        Ok(value.nfts.map { case (_, nft) => formatToNFTResponse(nft) }.toList)
      }.getOrElse(NotFound())
    }
  }

  def getCollectionNFTById(collectionId: String, nftId: Long): IO[Response[IO]] = {
    getState.flatMap { state =>
      state.collections.get(collectionId).flatMap { collection =>
        collection.nfts.get(nftId).map { nft => Ok(formatToNFTResponse(nft)) }
      }.getOrElse(NotFound())
    }
  }

  def getAllCollectionsOfAddress(address: Address): IO[Response[IO]] = {
    getState.flatMap { state =>
      val addressCollections = state.collections.filter { case (_, collection) =>
        collection.owner == address
      }
      Ok(addressCollections.map { case (_, collection) => formatToCollectionResponse(collection) })
    }
  }

  def getAllNFTsOfAddress(address: Address): IO[Response[IO]] = {
    getState.flatMap { state =>
      val allAddressNFTs = state.collections.flatMap {
        case (_, collection) =>
          val addressNFTs = collection.nfts.filter { case (_, nft) =>
            nft.owner == address
          }
          addressNFTs
      }
      Ok(allAddressNFTs.map { case (_, nft) => formatToNFTResponse(nft) })
    }
  }
}