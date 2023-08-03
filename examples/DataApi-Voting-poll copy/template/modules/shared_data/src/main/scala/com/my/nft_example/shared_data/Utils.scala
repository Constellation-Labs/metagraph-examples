package com.my.nft_example.shared_data

import com.my.nft_example.shared_data.Data.{NFTUpdate, State}
import io.circe.parser
import io.circe.syntax.EncoderOps

import java.net.{MalformedURLException, URISyntaxException, URL}
import java.nio.charset.StandardCharsets

object Utils {

  @throws[MalformedURLException]
  @throws[URISyntaxException]
  def isValidURL(url: String): Boolean = try {
    new URL(url).toURI
    true
  } catch {
    case _: MalformedURLException =>
      false
    case _: URISyntaxException =>
      false
  }

  def customUpdateSerialization(update: NFTUpdate): Array[Byte] = {
    println("Serialize UPDATE event received")
    println(update.asJson.deepDropNullValues.noSpaces)
    update.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
  }

  def customStateSerialization(state: State): Array[Byte] = {
    println("Serialize STATE event received")
    println(state.asJson.deepDropNullValues.noSpaces)
    state.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
  }

  def customStateDeserialization(bytes: Array[Byte]): Either[Throwable, State] = {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[State]
    }
  }

  def customUpdateDeserialization(bytes: Array[Byte]): Either[Throwable, NFTUpdate] = {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[NFTUpdate]
    }
  }
}

