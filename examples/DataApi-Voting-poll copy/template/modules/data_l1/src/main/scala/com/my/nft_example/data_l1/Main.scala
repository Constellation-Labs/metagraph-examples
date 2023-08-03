package com.my.nft_example.data_l1

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits.catsSyntaxValidatedIdBinCompat0
import com.my.nft_example.shared_data.Data
import com.my.nft_example.shared_data.Data.{NFTUpdate, State}
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.dsl.io._
import org.tessellation.BuildInfo
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{BaseDataApplicationL1Service, DataApplicationL1Service, L1NodeContext}
import org.tessellation.currency.l1.CurrencyL1App
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.security.signature.Signed

import java.util.UUID

object Main
  extends CurrencyL1App(
    "currency-data_l1",
    "currency data L1 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    version = BuildInfo.version
  ) {
  override def dataApplication: Option[BaseDataApplicationL1Service[IO]] = Option(BaseDataApplicationL1Service(new DataApplicationL1Service[IO, NFTUpdate, State] {

    override def serializeState(state: State): IO[Array[Byte]] = Data.serializeState(state)

    override def deserializeState(bytes: Array[Byte]): IO[Either[Throwable, State]] = Data.deserializeState(bytes)

    override def serializeUpdate(update: NFTUpdate): IO[Array[Byte]] = Data.serializeUpdate(update)

    override def deserializeUpdate(bytes: Array[Byte]): IO[Either[Throwable, NFTUpdate]] = Data.deserializeUpdate(bytes)

    override def dataEncoder: Encoder[NFTUpdate] = Data.dataEncoder

    override def dataDecoder: Decoder[NFTUpdate] = Data.dataDecoder

    override def validateData(oldState: State, updates: NonEmptyList[Signed[NFTUpdate]])(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = IO {
      ().validNec
    }

    override def validateUpdate(update: NFTUpdate)(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = Data.validateUpdate(update)

    override def combine(oldState: State, updates: NonEmptyList[Signed[NFTUpdate]])(implicit context: L1NodeContext[IO]): IO[State] = IO {
      oldState
    }

    override def routes(implicit context: L1NodeContext[IO]): HttpRoutes[IO] = HttpRoutes.empty
  }))
}
