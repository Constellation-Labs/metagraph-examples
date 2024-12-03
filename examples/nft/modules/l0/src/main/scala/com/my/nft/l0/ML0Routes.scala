package com.my.nft.l0

import cats.effect.Async
import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps

import org.tessellation.ext.http4s.AddressVar
import org.tessellation.schema.address.Address

import com.my.nft.shared_data.schema.{NFT, NFTCollection, NFTUpdatesCalculatedState}

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.constellationnetwork.metagraph_sdk.MetagraphPublicRoutes
import io.constellationnetwork.metagraph_sdk.lifecycle.CheckpointService
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder

case class CustomRoutes[F[_]: Async](
  checkpointService: CheckpointService[F, NFTUpdatesCalculatedState]
) extends MetagraphPublicRoutes[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "collections"                                 => getAllCollections
    case GET -> Root / "collections" / collectionId                  => getCollectionById(collectionId)
    case GET -> Root / "collections" / collectionId / "nfts"         => getCollectionNFTs(collectionId)
    case GET -> Root / "collections" / collectionId / "nfts" / nftId => getCollectionNFTById(collectionId, nftId.toLong)
    case GET -> Root / "addresses" / AddressVar(address) / "collections" => getAllCollectionsOfAddress(address)
    case GET -> Root / "addresses" / AddressVar(address) / "nfts"        => getAllNFTsOfAddress(address)
  }

  private def getAllCollections: F[Response[F]] =
    getState.flatMap { state =>
      val allCollectionsResponse = state.collections.map { case (_, collection) =>
        formatToCollectionResponse(collection)
      }.toList
      Ok(allCollectionsResponse)
    }

  private def getCollectionById(
    collectionId: String
  ): F[Response[F]] =
    getState.flatMap { state =>
      state.collections
        .get(collectionId)
        .map { value =>
          Ok(formatToCollectionResponse(value))
        }
        .getOrElse(NotFound())
    }

  private def getCollectionNFTs(
    collectionId: String
  ): F[Response[F]] =
    getState.flatMap { state =>
      state.collections
        .get(collectionId)
        .map { value =>
          Ok(value.nfts.map { case (_, nft) => formatToNFTResponse(nft) }.toList)
        }
        .getOrElse(NotFound())
    }

  private def formatToNFTResponse(
    nft: NFT
  ): NFTResponse =
    NFTResponse(
      nft.id,
      nft.collectionId,
      nft.owner,
      nft.uri,
      nft.name,
      nft.description,
      nft.creationDateTimestamp,
      nft.metadata
    )

  private def getCollectionNFTById(
    collectionId: String,
    nftId:        Long
  ): F[Response[F]] =
    getState.flatMap { state =>
      state.collections
        .get(collectionId)
        .flatMap { collection =>
          collection.nfts.get(nftId).map(nft => Ok(formatToNFTResponse(nft)))
        }
        .getOrElse(NotFound())
    }

  private def getAllCollectionsOfAddress(
    address: Address
  ): F[Response[F]] =
    getState.flatMap { state =>
      val addressCollections = state.collections.filter { case (_, collection) =>
        collection.owner == address
      }
      Ok(addressCollections.map { case (_, collection) => formatToCollectionResponse(collection) })
    }

  private def formatToCollectionResponse(
    collection: NFTCollection
  ): CollectionResponse =
    CollectionResponse(
      collection.id,
      collection.owner,
      collection.name,
      collection.creationDateTimestamp,
      collection.nfts.size.toLong
    )

  private def getState: F[NFTUpdatesCalculatedState] =
    checkpointService.get.map(_.state)

  private def getAllNFTsOfAddress(
    address: Address
  ): F[Response[F]] =
    getState.flatMap { state =>
      val allAddressNFTs = state.collections.flatMap { case (_, collection) =>
        val addressNFTs = collection.nfts.filter { case (_, nft) =>
          nft.owner == address
        }
        addressNFTs
      }
      Ok(allAddressNFTs.map { case (_, nft) => formatToNFTResponse(nft) })
    }
}

@derive(decoder, encoder)
case class NFTResponse(
  id:                    Long,
  collectionId:          String,
  owner:                 Address,
  uri:                   String,
  name:                  String,
  description:           String,
  creationDateTimestamp: Long,
  metadata:              Map[String, String]
)

@derive(decoder, encoder)
case class CollectionResponse(
  id:                    String,
  owner:                 Address,
  name:                  String,
  creationDateTimestamp: Long,
  numberOfNFTs:          Long
)
