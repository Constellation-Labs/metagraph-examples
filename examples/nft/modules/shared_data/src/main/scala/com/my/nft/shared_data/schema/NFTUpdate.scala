package com.my.nft.shared_data.schema

import cats.effect.Sync
import cats.syntax.functor._

import org.tessellation.currency.dataApplication.DataUpdate
import org.tessellation.schema.address.Address

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.constellationnetwork.metagraph_sdk.std.JsonBinaryCodec
import io.constellationnetwork.metagraph_sdk.std.JsonBinaryCodec.{deserializeDataUpdate, serializeDataUpdate}

@derive(decoder, encoder)
sealed trait NFTUpdate extends DataUpdate

@derive(decoder, encoder)
case class MintCollection(
  name: String
) extends NFTUpdate

@derive(decoder, encoder)
case class MintNFT(
  owner:        Address,
  collectionId: String,
  nftId:        Long,
  uri:          String,
  name:         String,
  description:  String,
  metadata:     Map[String, String]
) extends NFTUpdate

@derive(decoder, encoder)
case class TransferCollection(
  fromAddress:  Address,
  toAddress:    Address,
  collectionId: String
) extends NFTUpdate

@derive(decoder, encoder)
case class TransferNFT(
  fromAddress:  Address,
  toAddress:    Address,
  collectionId: String,
  nftId:        Long
) extends NFTUpdate

object NFTUpdate {

  implicit def transactionBinaryCodec[F[_]: Sync]: JsonBinaryCodec[F, NFTUpdate] =
    new JsonBinaryCodec[F, NFTUpdate] {

      override def serialize(obj: NFTUpdate): F[Array[Byte]] =
        serializeDataUpdate[F, NFTUpdate](obj)

      override def deserialize(bytes: Array[Byte]): F[Either[Throwable, NFTUpdate]] =
        deserializeDataUpdate[F, NFTUpdate](bytes).map {
          case Right(value: NFTUpdate) => Right(value)
          case Left(err)               => Left(err)
          case _                       => Left(new Exception("Unexpected result parsing DataUpdate"))
        }
    }
}
