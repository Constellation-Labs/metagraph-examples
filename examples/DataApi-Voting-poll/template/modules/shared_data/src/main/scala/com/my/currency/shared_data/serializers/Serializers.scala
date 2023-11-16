package com.my.currency.shared_data.serializers

import com.my.currency.shared_data.types.Types.{PollUpdate, VoteStateOnChain}
import io.circe.Encoder
import io.circe.syntax.EncoderOps
import org.slf4j.LoggerFactory
import org.tessellation.currency.dataApplication.DataUpdate
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationBlock
import org.tessellation.security.signature.Signed

import java.nio.charset.StandardCharsets

object Serializers {
  private val logger = LoggerFactory.getLogger("Serializers")
  def serializeUpdate(update: PollUpdate): Array[Byte] = {
    println("Serialize UPDATE event received")
    println(update.asJson.deepDropNullValues.noSpaces)
    update.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
  }

  def serializeState(state: VoteStateOnChain): Array[Byte] = {
    println("Serialize STATE event received")
    println(state.asJson.deepDropNullValues.noSpaces)
    state.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
  }

  def serializeBlock(state: Signed[DataApplicationBlock])(implicit e: Encoder[DataUpdate]): Array[Byte] = {
    val jsonState = state.asJson.deepDropNullValues.noSpaces
    logger.info(s"Serialize BLOCK event received: $jsonState")
    state.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
  }
}