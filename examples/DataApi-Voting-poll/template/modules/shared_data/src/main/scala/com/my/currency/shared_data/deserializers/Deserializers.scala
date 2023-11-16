package com.my.currency.shared_data.deserializers

import com.my.currency.shared_data.types.Types.{PollUpdate, VoteStateOnChain}
import io.circe.{Decoder, parser}
import org.tessellation.currency.dataApplication.DataUpdate
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationBlock
import org.tessellation.security.signature.Signed

import java.nio.charset.StandardCharsets

object Deserializers {

  def deserializeUpdate(bytes: Array[Byte]): Either[Throwable, PollUpdate] = {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[PollUpdate]
    }
  }

  def deserializeState(bytes: Array[Byte]): Either[Throwable, VoteStateOnChain] = {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[VoteStateOnChain]
    }
  }

  def deserializeBlock(bytes: Array[Byte])(implicit e: Decoder[DataUpdate]): Either[Throwable, Signed[DataApplicationBlock]] = {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[Signed[DataApplicationBlock]]
    }
  }
}