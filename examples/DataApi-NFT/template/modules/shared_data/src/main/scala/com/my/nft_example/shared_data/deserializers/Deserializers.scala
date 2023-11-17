package com.my.nft_example.shared_data.deserializers

import com.my.nft_example.shared_data.types.Types.{NFTUpdate, NFTUpdatesState}
import io.circe.{Decoder, parser}
import org.tessellation.currency.dataApplication.DataUpdate
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationBlock
import org.tessellation.security.signature.Signed

import java.nio.charset.StandardCharsets

object Deserializers {

  def deserializeUpdate(bytes: Array[Byte]): Either[Throwable, NFTUpdate] = {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[NFTUpdate]
    }
  }

  def deserializeState(bytes: Array[Byte]): Either[Throwable, NFTUpdatesState] = {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[NFTUpdatesState]
    }
  }

  def deserializeBlock(bytes: Array[Byte])(implicit e: Decoder[DataUpdate]): Either[Throwable, Signed[DataApplicationBlock]] = {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[Signed[DataApplicationBlock]]
    }
  }
}