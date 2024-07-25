package com.my.metagraph_social.shared_data.types.codecs

import com.my.metagraph_social.shared_data.types.Updates.SocialUpdate
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, HCursor, Json}
import org.tessellation.currency.dataApplication.DataUpdate

object DataUpdateCodec {
  implicit val dataUpdateEncoder: Encoder[DataUpdate] = {
    case event: SocialUpdate => event.asJson
    case _ => Json.Null
  }

  implicit val dataUpdateDecoder: Decoder[DataUpdate] = (c: HCursor) => c.as[SocialUpdate]
}
