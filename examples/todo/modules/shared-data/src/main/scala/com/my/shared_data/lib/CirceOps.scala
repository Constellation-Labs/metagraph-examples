package com.my.shared_data.lib

import org.tessellation.currency.dataApplication.DataUpdate

import com.my.shared_data.schema.Updates.TodoUpdate

import io.circe._
import io.circe.syntax.EncoderOps

object CirceOps {

  object implicits {

    implicit val dataUpateEncoder: Encoder[DataUpdate] = {
      case event: TodoUpdate => event.asJson
      case _                 => Json.Null
    }

    implicit val dataUpdateDecoder: Decoder[DataUpdate] = (c: HCursor) => c.as[TodoUpdate]
  }
}
