package my.weathermetagraph.shared.data.domain

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import doobie._
import io.circe.{Decoder, Encoder}
import my.weathermetagraph.shared.data.domain.Location._

import java.sql.Timestamp
import java.time.Instant

object Weather {

  @derive(encoder, decoder)
  object WeatherCondition extends Enumeration {

    type WeatherCondition = Value
    val Sunny, PartlyCloudy, Cloudy, Rainy, Snowy, Stormy, Turbulent = Value

    implicit val weatherConditionEncoder: Encoder[WeatherCondition] = Encoder.encodeString.contramap(_.toString)

    implicit val weatherConditionDecoder: Decoder[WeatherCondition] = Decoder.decodeString.emap { condition =>
      values.find(_.toString == condition).toRight(s"Invalid WeatherCondition: $condition")
    }
  }

  import WeatherCondition._

  @derive(encoder, decoder)
  case class WeatherRecord(
    id:              String,
    snapshotOrdinal: Long,
    location:        LocationRecord,
    tempF:           Float,
    tempC:           Float,
    condition:       WeatherCondition,
    recordDate:      Instant)

  implicit val instantMeta: Meta[Instant] =
    Meta[Timestamp].timap(_.toInstant)(Timestamp.from)

  implicit val weatherWrite: Write[WeatherRecord] =
    Write[(String, Long, String, Float, Float, String, Instant)].contramap { record =>
      (record.id, record.snapshotOrdinal, record.location.id, record.tempF, record.tempC, record.condition.toString, record.recordDate)
    }

  implicit val weatherRead: Read[WeatherRecord] =
    Read[(String, Long, String, String, String, String, Float, Float, String, Instant)].map {
      case (id, snapshotOrdinal, locationId, locationName, locationRegion, locationCountry, tempF, tempC, condition, date) =>
        WeatherRecord(
          id = id,
          snapshotOrdinal = snapshotOrdinal,
          location = LocationRecord(locationId, locationName, locationRegion, locationCountry),
          tempF = tempF,
          tempC = tempC,
          condition = WeatherCondition.withName(condition),
          recordDate = date
        )
    }
}
