package my.weathermetagraph.shared.data.domain

import cats.effect.Sync
import cats.syntax.functor._
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.constellationnetwork.metagraph_sdk.std.JsonBinaryCodec
import my.weathermetagraph.shared.data.domain.Account.AccountState
import my.weathermetagraph.shared.data.domain.Location.LocationRecord
import my.weathermetagraph.shared.data.domain.Request.RequestUpdate
import my.weathermetagraph.shared.data.domain.Weather.WeatherCondition.WeatherCondition
import my.weathermetagraph.shared.data.domain.Weather.WeatherRecord
import org.tessellation.currency.dataApplication.{DataCalculatedState, DataOnChainState, DataUpdate}
import org.tessellation.schema.SnapshotOrdinal

import java.time.Instant

object MetagraphState {

  @derive(encoder, decoder)
  sealed trait WeatherDataUpdate extends DataUpdate

  @derive(encoder, decoder)
  sealed trait WeatherDataCalculatedState {
    def updates: List[WeatherDataUpdate]
  }

  @derive(encoder, decoder)
  sealed trait WeatherDataOnChainState {
    def updates: List[WeatherDataUpdate]
  }

  @derive(encoder, decoder)
  case class WeatherUpdate(
    location:  LocationRecord,
    tempF:     Float,
    tempC:     Float,
    condition: WeatherCondition,
    date:      Instant)
      extends WeatherDataUpdate {

    def toRecord(
      snapshotOrdinal: SnapshotOrdinal
    ): WeatherRecord =
      WeatherRecord(
        id = java.util.UUID.randomUUID().toString,
        snapshotOrdinal = snapshotOrdinal.value.value,
        location = location,
        tempF = tempF,
        tempC = tempC,
        condition = condition,
        recordDate = date
      )
  }

  @derive(encoder, decoder)
  case class AccountRequestsUpdate(
    account:  AccountState,
    requests: List[RequestUpdate])
      extends WeatherDataUpdate

  @derive(encoder, decoder)
  case class CalculatedState(
    states: List[WeatherDataCalculatedState])
      extends DataCalculatedState

  object CalculatedState {
    val genesis: CalculatedState = CalculatedState(List.empty)
  }

  @derive(encoder, decoder)
  case class OnChainState(
    states: List[WeatherDataOnChainState])
      extends DataOnChainState

  object OnChainState {
    val genesis: OnChainState = OnChainState(List.empty)
  }

  @derive(encoder, decoder)
  case class WeatherCalculatedState(
    updates: List[WeatherUpdate])
      extends WeatherDataCalculatedState

  @derive(encoder, decoder)
  case class RequestCalculatedState(
    updates: List[AccountRequestsUpdate])
      extends WeatherDataCalculatedState

  @derive(encoder, decoder)
  case class WeatherOnChainState(
    updates: List[WeatherUpdate])
      extends WeatherDataOnChainState

  @derive(encoder, decoder)
  case class RequestOnChainState(
    updates: List[AccountRequestsUpdate])
      extends WeatherDataOnChainState

  implicit def weatherDataUpdateCodec[F[_]: Sync]: JsonBinaryCodec[F, WeatherDataUpdate] =
    new JsonBinaryCodec[F, WeatherDataUpdate] {

      override def serialize(
        dataUpdate: WeatherDataUpdate
      ): F[Array[Byte]] = JsonBinaryCodec.serializeDataUpdate(dataUpdate)

      override def deserialize(
        bytes: Array[Byte]
      ): F[Either[Throwable, WeatherDataUpdate]] = JsonBinaryCodec.deserializeDataUpdate[F, WeatherDataUpdate](bytes).map {
        case Right(value: WeatherDataUpdate) => Right(value)
        case Left(err)                       => Left(err)
        case _                               => Left(new Exception("Unexpected result parsing DataUpdate"))
      }
    }

  implicit def calculatedStateCodec[F[_]: Sync]: JsonBinaryCodec[F, CalculatedState] =
    new JsonBinaryCodec[F, CalculatedState] {

      override def serialize(
        dataUpdate: CalculatedState
      ): F[Array[Byte]] = JsonBinaryCodec.simpleJsonSerialization(dataUpdate)

      override def deserialize(
        bytes: Array[Byte]
      ): F[Either[Throwable, CalculatedState]] = JsonBinaryCodec.simpleJsonDeserialization(bytes)
    }

  implicit def onChainStateCodec[F[_]: Sync]: JsonBinaryCodec[F, OnChainState] =
    new JsonBinaryCodec[F, OnChainState] {

      override def serialize(
        dataUpdate: OnChainState
      ): F[Array[Byte]] = JsonBinaryCodec.simpleJsonSerialization(dataUpdate)

      override def deserialize(
        bytes: Array[Byte]
      ): F[Either[Throwable, OnChainState]] = JsonBinaryCodec.simpleJsonDeserialization(bytes)
    }

}
