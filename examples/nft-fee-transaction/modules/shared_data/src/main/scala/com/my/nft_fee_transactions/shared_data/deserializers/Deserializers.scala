package com.my.nft_fee_transactions.shared_data.deserializers

import com.my.nft_fee_transactions.shared_data.types.Types._
import io.circe.Decoder
import io.circe.jawn.decode
import io.constellationnetwork.currency.dataApplication.DataUpdate
import io.constellationnetwork.currency.dataApplication.dataApplication.DataApplicationBlock
import io.constellationnetwork.security.signature.Signed

import java.nio.charset.StandardCharsets

object Deserializers {
  private def deserialize[A: Decoder](
    bytes: Array[Byte]
  ): Either[Throwable, A] =
    decode[A](new String(bytes, StandardCharsets.UTF_8))

  def deserializeUpdate(
    bytes: Array[Byte]
  ): Either[Throwable, NFTUpdate] =
    deserialize[NFTUpdate](bytes)

  def deserializeState(
    bytes: Array[Byte]
  ): Either[Throwable, NFTUpdatesState] =
    deserialize[NFTUpdatesState](bytes)

  def deserializeBlock(
    bytes: Array[Byte]
  )(implicit e: Decoder[DataUpdate]): Either[Throwable, Signed[DataApplicationBlock]] =
    deserialize[Signed[DataApplicationBlock]](bytes)

  def deserializeCalculatedState(
    bytes: Array[Byte]
  ): Either[Throwable, NFTUpdatesCalculatedState] =
    deserialize[NFTUpdatesCalculatedState](bytes)
}