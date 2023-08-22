package com.my.currency.shared_data

import com.my.currency.shared_data.Types.{ UsageState, UsageUpdate}
import io.circe.parser
import io.circe.syntax.EncoderOps
import org.tessellation.security.hash.Hash

import java.nio.charset.StandardCharsets

object Utils {

  def getUsageUpdateHash(update: UsageUpdate): String = Hash.fromBytes(customUpdateSerialization(update)).toString
  def customUpdateSerialization(update: UsageUpdate): Array[Byte] = {
    println("Serialize UPDATE event received")
    println(update.asJson.deepDropNullValues.noSpaces)
    update.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
  }

  def customStateSerialization(state: UsageState): Array[Byte] = {
    println("Serialize STATE event received")
    println(state.asJson.deepDropNullValues.noSpaces)
    state.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
  }

  def customStateDeserialization(bytes: Array[Byte]): Either[Throwable, UsageState] = {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[UsageState]
    }
  }

  def customUpdateDeserialization(bytes: Array[Byte]): Either[Throwable, UsageUpdate] = {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[UsageUpdate]
    }
  }
}
