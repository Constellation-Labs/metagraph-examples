package com.my.nft.shared_data.schema

import cats.effect.Sync

import org.tessellation.currency.dataApplication.DataOnChainState

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.constellationnetwork.metagraph_sdk.std.JsonBinaryCodec
import io.constellationnetwork.metagraph_sdk.std.JsonBinaryCodec.{simpleJsonDeserialization, simpleJsonSerialization}

@derive(decoder, encoder)
case class NFTUpdatesState(updates: List[NFTUpdate]) extends DataOnChainState

object NFTUpdatesState {
  val genesis: NFTUpdatesState = NFTUpdatesState(List.empty)

  implicit def onchainBinaryCodec[F[_]: Sync]: JsonBinaryCodec[F, NFTUpdatesState] =
    new JsonBinaryCodec[F, NFTUpdatesState] {

      override def serialize(obj: NFTUpdatesState): F[Array[Byte]] =
        simpleJsonSerialization(obj)

      override def deserialize(bytes: Array[Byte]): F[Either[Throwable, NFTUpdatesState]] =
        simpleJsonDeserialization(bytes)
    }
}
