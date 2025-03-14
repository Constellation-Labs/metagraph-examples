package my.weathermetagraph.shared.data.domain

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import doobie._

object Location {

  @derive(encoder, decoder)
  case class LocationRecord(
    id:      String,
    name:    String,
    region:  String,
    country: String)

  implicit val locationRead: Read[LocationRecord] =
    Read[(String, String, String, String)].map {
      case (id, name, region, country) =>
        LocationRecord(id, name, region, country)
    }

  implicit val locationWrite: Write[LocationRecord] =
    Write[(String, String, String, String)].contramap { location =>
      (location.id, location.name, location.region, location.country)
    }
}
