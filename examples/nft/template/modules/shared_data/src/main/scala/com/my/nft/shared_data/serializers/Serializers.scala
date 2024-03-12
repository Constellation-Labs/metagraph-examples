package com.my.nft.shared_data.serializers

import com.my.nft.shared_data.types.Types._
import io.circe.Encoder
import io.circe.syntax.EncoderOps
import org.tessellation.currency.dataApplication.DataUpdate
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationBlock
import org.tessellation.security.signature.Signed

import java.nio.charset.StandardCharsets

object Serializers {
  private def serialize[A: Encoder](
    serializableData: A
  ): Array[Byte] = {
    serializableData.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
  }

  def serializeUpdate(
    update: NFTUpdate
  ): Array[Byte] =
    serialize[NFTUpdate](update)

  def serializeState(
    state: NFTUpdatesState
  ): Array[Byte] =
    serialize[NFTUpdatesState](state)

  def serializeBlock(
    state: Signed[DataApplicationBlock]
  )(implicit e: Encoder[DataUpdate]): Array[Byte] =
    serialize[Signed[DataApplicationBlock]](state)

  def serializeCalculatedState(
    state: NFTUpdatesCalculatedState
  ): Array[Byte] =
    serialize[NFTUpdatesCalculatedState](state)
}