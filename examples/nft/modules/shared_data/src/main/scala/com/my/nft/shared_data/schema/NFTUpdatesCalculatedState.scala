package com.my.nft.shared_data.schema

import cats.effect.Sync

import org.tessellation.currency.dataApplication.DataCalculatedState

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.constellationnetwork.metagraph_sdk.std.JsonBinaryCodec
import io.constellationnetwork.metagraph_sdk.std.JsonBinaryCodec.{simpleJsonDeserialization, simpleJsonSerialization}

@derive(decoder, encoder)
case class NFTUpdatesCalculatedState(collections: Map[String, NFTCollection]) extends DataCalculatedState

object NFTUpdatesCalculatedState {
  val genesis: NFTUpdatesCalculatedState = NFTUpdatesCalculatedState(Map.empty)

  implicit def calculatedBinaryCodec[F[_]: Sync]: JsonBinaryCodec[F, NFTUpdatesCalculatedState] =
    new JsonBinaryCodec[F, NFTUpdatesCalculatedState] {

      override def serialize(obj: NFTUpdatesCalculatedState): F[Array[Byte]] =
        simpleJsonSerialization(obj)

      override def deserialize(bytes: Array[Byte]): F[Either[Throwable, NFTUpdatesCalculatedState]] =
        simpleJsonDeserialization(bytes)
    }
}
