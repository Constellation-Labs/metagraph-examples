package com.my.currency.data_l1

import cats.data.{NonEmptyList, OptionT}
import cats.effect.IO
import cats.implicits.catsSyntaxOption
import com.my.currency.shared_data.Data
import com.my.currency.shared_data.Data.{State, Update}
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.dsl.io._
import org.tessellation.BuildInfo
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{BaseDataApplicationL1Service, DataApplicationL1Service, L1NodeContext}
import org.tessellation.currency.l1.CurrencyL1App
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.security.signature.Signed
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.tessellation.ext.http4s.AddressVar
import org.tessellation.schema.address.Address

import java.nio.charset.StandardCharsets
import java.util.UUID

object Main
  extends CurrencyL1App(
    "currency-data_l1",
    "currency data L1 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    version = BuildInfo.version
  ) {
  override def dataApplication: Option[BaseDataApplicationL1Service[IO]] = Option(BaseDataApplicationL1Service(new DataApplicationL1Service[IO, Update, State] {

    override def serializeState(state: State): IO[Array[Byte]] = Data.serializeState(state)

    override def deserializeState(bytes: Array[Byte]): IO[Either[Throwable, State]] = Data.deserializeState(bytes)

    override def serializeUpdate(update: Update): IO[Array[Byte]] = Data.serializeUpdate(update)

    override def deserializeUpdate(bytes: Array[Byte]): IO[Either[Throwable, Update]] = Data.deserializeUpdate(bytes)

    override def dataEncoder: Encoder[Update] = Data.dataEncoder

    override def dataDecoder: Decoder[Update] = Data.dataDecoder

    override def validateData(oldState: State, updates: NonEmptyList[Signed[Update]])(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = Data.validateData(oldState, updates)(context.securityProvider)

    override def validateUpdate(update: Update)(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = Data.validateUpdate(update)

    override def combine(oldState: State, updates: NonEmptyList[Signed[Update]])(implicit context: L1NodeContext[IO]): IO[State] = Data.combine(oldState, updates)

    /*
    * This function will implement custom endpoints to our Data API. In this example, we will implement 2 different endpoints:
    * -> The first one will list all the devices of the state
    * -> The second one will show the information of the device of the provided address, or Not Found
    */
    override def routes(implicit context: L1NodeContext[IO]): HttpRoutes[IO] = HttpRoutes.of {
      case GET -> Root / "addresses" =>
        OptionT(context.getLastCurrencySnapshot)
          .flatMap(_.data.toOptionT)
          .flatMapF(deserializeState(_).map(_.toOption))
          .value
          .flatMap {
            case Some(value) =>
              Ok(value.devices)
            case None        =>
              NotFound()
          }

      case GET -> Root / "addresses" / AddressVar(address) =>
        OptionT(context.getLastCurrencySnapshot)
          .flatMap(_.data.toOptionT)
          .flatMapF(deserializeState(_).map(_.toOption))
          .value
          .flatMap {
            case Some(value) =>
              val stateAddress = value.devices.get(address)
              stateAddress match {
                case Some(value) =>
                  Ok(value)
                case None =>
                  NotFound()
              }
            case None =>
              NotFound()
          }
    }
  }))
}
