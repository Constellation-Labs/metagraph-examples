package com.my.currency.l0

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxValidatedIdBinCompat0}
import com.my.currency.shared_data.Data
import com.my.currency.shared_data.Types.{UsageState, UsageUpdate}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.{EntityDecoder, HttpRoutes}
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
    Option(BaseDataApplicationL0Service(new DataApplicationL0Service[IO, UsageUpdate, UsageState] {

      override def genesis: UsageState = UsageState(Map.empty, Map.empty, Map.empty, Map.empty)

      override def validateData(oldState: UsageState, updates: NonEmptyList[Signed[UsageUpdate]])(implicit context: L0NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = Data.validateData(oldState, updates)(context.securityProvider)

      override def validateUpdate(update: UsageUpdate)(implicit context: L0NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = ().validNec.pure[IO]

      override def combine(oldState: UsageState, updates: NonEmptyList[Signed[UsageUpdate]])(implicit context: L0NodeContext[IO]): IO[UsageState] = Data.combine(oldState, updates)

      override def serializeState(state: UsageState): IO[Array[Byte]] = Data.serializeState(state)

      override def deserializeState(bytes: Array[Byte]): IO[Either[Throwable, UsageState]] = Data.deserializeState(bytes)

      override def serializeUpdate(update: UsageUpdate): IO[Array[Byte]] = Data.serializeUpdate(update)

      override def deserializeUpdate(bytes: Array[Byte]): IO[Either[Throwable, UsageUpdate]] = Data.deserializeUpdate(bytes)

      override def dataEncoder: Encoder[UsageUpdate] = Data.dataEncoder

      override def dataDecoder: Decoder[UsageUpdate] = Data.dataDecoder

      override def routes(implicit context: L0NodeContext[IO]): HttpRoutes[IO] = HttpRoutes.empty

      override def signedDataEntityDecoder: EntityDecoder[IO, Signed[UsageUpdate]] = circeEntityDecoder
    }))

  def rewards(implicit sp: SecurityProvider[IO]) = None
}
