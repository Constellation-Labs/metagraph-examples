package com.my.nft_example.shared_data.serializers

import com.my.nft_example.shared_data.types.Types.{NFTUpdate, NFTUpdatesState}
import io.circe.Encoder
import io.circe.syntax.EncoderOps
import org.slf4j.LoggerFactory
import org.tessellation.currency.dataApplication.DataUpdate
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationBlock
import org.tessellation.security.signature.Signed

import java.nio.charset.StandardCharsets

object Serializers {
  private val logger = LoggerFactory.getLogger("Serializers")
  def serializeUpdate(update: NFTUpdate): Array[Byte] = {
    val jsonState = update.asJson.deepDropNullValues.noSpaces
    logger.info(s"Serialize UPDATE event received: $jsonState")
    jsonState.getBytes(StandardCharsets.UTF_8)
  }

  def serializeState(state: NFTUpdatesState): Array[Byte] = {
    val jsonState = state.asJson.deepDropNullValues.noSpaces
    logger.info(s"Serialize STATE event received: $jsonState")
    jsonState.getBytes(StandardCharsets.UTF_8)
  }

  def serializeBlock(state: Signed[DataApplicationBlock])(implicit e: Encoder[DataUpdate]): Array[Byte] = {
    val jsonState = state.asJson.deepDropNullValues.noSpaces
    logger.info(s"Serialize BLOCK event received: $jsonState")
    state.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
  }
}