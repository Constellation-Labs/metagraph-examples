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

import scala.collection.mutable.ListBuffer

object CustomRoutes {

  @derive(decoder, encoder)
  case class CollectionResponse(id: String, owner: Address, name: String, creationDateTimestamp: Long, numberOfNFTs: Long)

  @derive(decoder, encoder)
  case class NFTResponse(id: Long, collectionId: String, owner: Address, uri: String, name: String, description: String, creationDateTimestamp: Long, metadata: Map[String, String])

  private def formatToCollectionResponse(collection: CollectionState): CollectionResponse = {
    CollectionResponse(collection.id, collection.owner, collection.name, collection.creationDateTimestamp, collection.nfts.count(_ => true).toLong)
  }

  private def formatToNFTResponse(nft: NFTState): NFTResponse = {
    NFTResponse(nft.id, nft.collectionId, nft.owner, nft.uri, nft.name, nft.description, nft.creationDateTimestamp, nft.metadata)
  }

  def getAllCollections(implicit context: L1NodeContext[IO]): IO[Response[IO]] = {
    OptionT(context.getLastCurrencySnapshot)
      .flatMap(_.data.toOptionT)
      .flatMapF(deserializeState(_).map(_.toOption))
      .value
      .flatMap {
        case Some(value) =>
          val allCollectionsResponse = value.collections.map { case (_, collection) => formatToCollectionResponse(collection) }.toList
          Ok(allCollectionsResponse)
        case None =>
          NotFound()
      }
  }

  def getCollectionById(collectionId: String)(implicit context: L1NodeContext[IO]): IO[Response[IO]] = {
    OptionT(context.getLastCurrencySnapshot)
      .flatMap(_.data.toOptionT)
      .flatMapF(deserializeState(_).map(_.toOption))
      .value
      .flatMap {
        case Some(value) =>
          val collection = value.collections.get(collectionId)
          collection match {
            case Some(value) => Ok(formatToCollectionResponse(value))
            case None => NotFound()
          }
        case None =>
          NotFound()
      }
  }

  def getCollectionNFTs(collectionId: String)(implicit context: L1NodeContext[IO]): IO[Response[IO]] = {
    OptionT(context.getLastCurrencySnapshot)
      .flatMap(_.data.toOptionT)
      .flatMapF(deserializeState(_).map(_.toOption))
      .value
      .flatMap {
        case Some(value) =>
          val collection = value.collections.get(collectionId)
          collection match {
            case Some(value) =>
              Ok(value.nfts.map { case (_, nft) => formatToNFTResponse(nft) }.toList)
            case None => NotFound()
          }
        case None =>
          NotFound()
      }
  }

  def getCollectionNFTById(collectionId: String, nftId: Long)(implicit context: L1NodeContext[IO]): IO[Response[IO]] = {
    OptionT(context.getLastCurrencySnapshot)
      .flatMap(_.data.toOptionT)
      .flatMapF(deserializeState(_).map(_.toOption))
      .value
      .flatMap {
        case Some(value) =>
          val collection = value.collections.get(collectionId)
          collection match {
            case Some(value) =>
              value.nfts.get(nftId) match {
                case Some(nft) => Ok(formatToNFTResponse(nft))
                case None => NotFound()
              }
            case None => NotFound()
          }
        case None =>
          NotFound()
      }
  }

  def getAllCollectionsOfAddress(address: Address)(implicit context: L1NodeContext[IO]): IO[Response[IO]] = {
    val allAddressCollections = ListBuffer[CollectionResponse]()
    OptionT(context.getLastCurrencySnapshot)
      .flatMap(_.data.toOptionT)
      .flatMapF(deserializeState(_).map(_.toOption))
      .value
      .flatMap {
        case Some(value) =>
          value.collections.map {
            case (_, collection) =>
              if (collection.owner == address) {
                allAddressCollections += formatToCollectionResponse(collection)
              }
          }
          Ok(allAddressCollections)
        case None =>
          NotFound()
      }
  }

  def getAllNFTsOfAddress(address: Address)(implicit context: L1NodeContext[IO]): IO[Response[IO]] = {
    val allAddressNFTs = ListBuffer[NFTResponse]()
    OptionT(context.getLastCurrencySnapshot)
      .flatMap(_.data.toOptionT)
      .flatMapF(deserializeState(_).map(_.toOption))
      .value
      .flatMap {
        case Some(value) =>
          value.collections.map {
            case (_, collection) =>
              collection.nfts.map {
                case (_, nft) =>
                  if (nft.owner == address) {
                    allAddressNFTs += formatToNFTResponse(nft)
                  }
              }
          }
          Ok(allAddressNFTs)
        case None =>
          NotFound()
      }
  }
}