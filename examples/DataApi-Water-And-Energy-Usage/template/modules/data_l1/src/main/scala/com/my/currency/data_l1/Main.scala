package com.my.currency.data_l1

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import com.my.currency.data_l1.CustomRoutes.{getAllDevices, getDeviceByAddress, getDeviceTransactions}
import com.my.currency.shared_data.Data
import com.my.currency.shared_data.Types.{UsageState, UsageUpdate}
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.dsl.io._
import org.tessellation.BuildInfo
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{BaseDataApplicationL1Service, DataApplicationL1Service, L1NodeContext}
import org.tessellation.currency.l1.CurrencyL1App
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.security.signature.Signed
import org.tessellation.ext.http4s.AddressVar
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder

import java.util.UUID

object Main
  extends CurrencyL1App(
    "currency-data_l1",
    "currency data L1 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    version = BuildInfo.version
  ) {
  override def dataApplication: Option[BaseDataApplicationL1Service[IO]] = Option(BaseDataApplicationL1Service(new DataApplicationL1Service[IO, UsageUpdate, UsageState] {

    override def serializeState(state: UsageState): IO[Array[Byte]] = Data.serializeState(state)

    override def deserializeState(bytes: Array[Byte]): IO[Either[Throwable, UsageState]] = Data.deserializeState(bytes)

    override def serializeUpdate(update: UsageUpdate): IO[Array[Byte]] = Data.serializeUpdate(update)

    override def deserializeUpdate(bytes: Array[Byte]): IO[Either[Throwable, UsageUpdate]] = Data.deserializeUpdate(bytes)

    override def dataEncoder: Encoder[UsageUpdate] = Data.dataEncoder

    override def dataDecoder: Decoder[UsageUpdate] = Data.dataDecoder

    override def validateData(oldState: UsageState, updates: NonEmptyList[Signed[UsageUpdate]])(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = Data.validateData(oldState, updates)(context.securityProvider)

    override def validateUpdate(update: UsageUpdate)(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = Data.validateUpdate(update)

    override def combine(oldState: UsageState, updates: NonEmptyList[Signed[UsageUpdate]])(implicit context: L1NodeContext[IO]): IO[UsageState] = oldState.pure[IO]

    override def routes(implicit context: L1NodeContext[IO]): HttpRoutes[IO] = HttpRoutes.of {
      case GET -> Root / "addresses" => getAllDevices()
      case GET -> Root / "addresses" / AddressVar(address) => getDeviceByAddress(address)
      case GET -> Root / "addresses" / AddressVar(address) / "transactions" => getDeviceTransactions(address)
    }

    override def signedDataEntityDecoder: EntityDecoder[IO, Signed[UsageUpdate]] = circeEntityDecoder
  }))
}
