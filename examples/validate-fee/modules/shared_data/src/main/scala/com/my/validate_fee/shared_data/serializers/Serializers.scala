package com.my.validate_fee.shared_data.serializers

import com.my.validate_fee.shared_data.types.Types.{UpdateWithFee, CalculatedUpdateWithFeeState, UpdateWithFeeChainState}
import io.circe.Encoder
import io.circe.syntax.EncoderOps
import org.tessellation.currency.dataApplication.DataUpdate
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationBlock
import org.tessellation.security.signature.Signed

import java.nio.charset.StandardCharsets
import java.util.Base64

object Serializers {
  def serialize[A: Encoder](serializableData: A): Array[Byte] =
    serializableData.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)

  def serializeUpdate(update: UpdateWithFee): Array[Byte] = {
    val encoder = Base64.getEncoder
    val data_sign_prefix = "\u0019Constellation Signed Data:\n"

    val updateBytes = update.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
    val encodedBytes = encoder.encode(updateBytes)

    val encodedString = new String(encodedBytes, "UTF-8")
    val completeString = s"$data_sign_prefix${encodedString.length}\n$encodedString"

    completeString.getBytes(StandardCharsets.UTF_8)
  }

  def serializeState(state: UpdateWithFeeChainState): Array[Byte] = {
    serialize[UpdateWithFeeChainState](state)
  }

  def serializeBlock(state: Signed[DataApplicationBlock])(implicit e: Encoder[DataUpdate]): Array[Byte] = {
    serialize[Signed[DataApplicationBlock]](state)
  }

  def serializeCalculatedState(state: CalculatedUpdateWithFeeState): Array[Byte] = {
    serialize[CalculatedUpdateWithFeeState](state)
  }
}