package my.weathermetagraph.shared.data.domain

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

object Request {

  @derive(encoder, decoder)
  case class RequestUpdate(
    endpoint:  String,
    cost:      Int,
    timestamp: Long)

}
