package com.my.nft_example.data_l1

import cats.data.OptionT
import cats.effect.IO
import cats.implicits.catsSyntaxOption
import com.my.nft_example.shared_data.Data.{CollectionState, NFTState, deserializeState}
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.tessellation.currency.dataApplication.L1NodeContext
import org.tessellation.schema.address.Address

object CustomRoutes {

  @derive(decoder, encoder)
  case class CollectionResponse(id: String, owner: Address, name: String, creationDateTimestamp: Long, numberOfNFTs: Long)

  @derive(decoder, encoder)
  case class NFTResponse(id: Long, collectionId: String, owner: Address, uri: String, name: String, description: String, creationDateTimestamp: Long, metadata: Map[String, String])

  private def formatToCollectionResponse(collection: CollectionState): CollectionResponse = {
    CollectionResponse(collection.id, collection.owner, collection.name, collection.creationDateTimestamp, collection.nfts.size.toLong)
  }

  private def formatToNFTResponse(nft: NFTState): NFTResponse = {
    NFTResponse(nft.id, nft.collectionId, nft.owner, nft.uri, nft.name, nft.description, nft.creationDateTimestamp, nft.metadata)
  }

  private def getState(context: L1NodeContext[IO]) = {
    OptionT(context.getLastCurrencySnapshot)
      .flatMap(_.data.toOptionT)
      .flatMapF(deserializeState(_).map(_.toOption))
      .value
  }

  def getAllCollections(implicit context: L1NodeContext[IO]): IO[Response[IO]] = {
    getState(context).flatMap {
      case None => NotFound()
      case Some(value) =>
        val allCollectionsResponse = value.collections.map { case (_, collection) => formatToCollectionResponse(collection) }.toList
        Ok(allCollectionsResponse)
    }
  }

  def getCollectionById(collectionId: String)(implicit context: L1NodeContext[IO]): IO[Response[IO]] = {
    getState(context).flatMap {
      case None => NotFound()
      case Some(value) =>
        value.collections.get(collectionId).map { value =>
          Ok(formatToCollectionResponse(value))
        }.getOrElse(NotFound())
    }
  }

  def getCollectionNFTs(collectionId: String)(implicit context: L1NodeContext[IO]): IO[Response[IO]] = {
    getState(context).flatMap {
      case None => NotFound()
      case Some(value) =>
        value.collections.get(collectionId).map { value =>
          Ok(value.nfts.map { case (_, nft) => formatToNFTResponse(nft) }.toList)
        }.getOrElse(NotFound())
    }
  }

  def getCollectionNFTById(collectionId: String, nftId: Long)(implicit context: L1NodeContext[IO]): IO[Response[IO]] = {
    getState(context).flatMap {
      case None => NotFound()
      case Some(state) =>
        state.collections.get(collectionId).flatMap { collection =>
          collection.nfts.get(nftId).map { nft => Ok(formatToNFTResponse(nft)) }
        }.getOrElse(NotFound())
    }
  }

  def getAllCollectionsOfAddress(address: Address)(implicit context: L1NodeContext[IO]): IO[Response[IO]] = {
    getState(context).flatMap {
      case None => NotFound()
      case Some(value) =>
        val addressCollections = value.collections.filter { case (_, collection) =>
          collection.owner == address
        }
        Ok(addressCollections.map { case (_, collection) => formatToCollectionResponse(collection) })
    }
  }

  def getAllNFTsOfAddress(address: Address)(implicit context: L1NodeContext[IO]): IO[Response[IO]] = {
    getState(context).flatMap {
      case None => NotFound()
      case Some(value) =>
        val allAddressNFTs = value.collections.flatMap {
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