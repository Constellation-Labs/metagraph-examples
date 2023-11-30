package com.my.nft_example.shared_data.deserializers

import com.my.nft_example.shared_data.types.Types.{NFTUpdate, NFTUpdatesCalculatedState, NFTUpdatesState}
import io.circe.{Decoder, parser}
import org.tessellation.currency.dataApplication.DataUpdate
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationBlock
import org.tessellation.security.signature.Signed

import java.nio.charset.StandardCharsets

object Deserializers {
  private def deserialize[A: Decoder](
    bytes: Array[Byte]
  ): Either[Throwable, A] =
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[A]
    }

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