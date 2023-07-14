package com.my.currency.l0

import cats.data.NonEmptyList
import cats.effect.IO
import com.my.currency.shared_data.Data
import com.my.currency.shared_data.Data.{State, Update}
import io.circe.{Decoder, Encoder}
import org.http4s.HttpRoutes
import org.tessellation.BuildInfo
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{BaseDataApplicationL0Service, DataApplicationL0Service, L0NodeContext}
import org.tessellation.currency.l0.CurrencyL0App
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.security.SecurityProvider
import org.tessellation.security.signature.Signed

import java.util.UUID

object Main
  extends CurrencyL0App(
    "currency-l0",
    "currency L0 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    version = BuildInfo.version
  ) {
  def dataApplication: Option[BaseDataApplicationL0Service[IO]] =
    Option(BaseDataApplicationL0Service(new DataApplicationL0Service[IO, Update, State] {

    /**
     * This is the initial State of your application. In this case, we will have a relationship between devices
     * and their usages of Energy and Water. For that, we should initialize our state with an empty map, imagine
     * this as a JSON like this:
     * { "devices" : {} }
     * initially, we don't have any devices, but according with the updates we will update this state to contain
     * devices, and the State will be like:
     * { "devices" : { "DAG8py4LY1sr8ZZM3aryeP85NuhgsCYcPKuhhbw6": { "waterUsage": { "usage": 10, "timestamp": 10 }, "energyUsage": { "usage": 100, "timestamp": 21 } } } }
     *
     */
    override def genesis: State = State(Map.empty)

    override def validateData(oldState: State, updates: NonEmptyList[Signed[Update]])(implicit context: L0NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = Data.validateData(oldState, updates)(context.securityProvider)

    override def validateUpdate(update: Update)(implicit context: L0NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = Data.validateUpdate(update)

    override def combine(oldState: State, updates: NonEmptyList[Signed[Update]])(implicit context: L0NodeContext[IO]): IO[State] = Data.combine(oldState, updates)

    override def serializeState(state: State): IO[Array[Byte]] = Data.serializeState(state)

    override def deserializeState(bytes: Array[Byte]): IO[Either[Throwable, State]] = Data.deserializeState(bytes)

    override def serializeUpdate(update: Update): IO[Array[Byte]] = Data.serializeUpdate(update)

    override def deserializeUpdate(bytes: Array[Byte]): IO[Either[Throwable, Update]] = Data.deserializeUpdate(bytes)

    override def dataEncoder: Encoder[Update] = Data.dataEncoder

    override def dataDecoder: Decoder[Update] = Data.dataDecoder

    override def routes(implicit context: L0NodeContext[IO]): HttpRoutes[IO] = HttpRoutes.empty

    }))

  def rewards(implicit sp: SecurityProvider[IO]) = None
}
