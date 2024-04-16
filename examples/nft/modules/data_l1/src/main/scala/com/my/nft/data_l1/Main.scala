package com.my.nft.data_l1

import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.option.catsSyntaxOptionId
import com.my.nft.shared_data.LifecycleSharedFunctions
import com.my.nft.shared_data.calculated_state.CalculatedStateService
import com.my.nft.shared_data.deserializers.Deserializers
import com.my.nft.shared_data.errors.Errors.valid
import com.my.nft.shared_data.serializers.Serializers
import com.my.nft.shared_data.types.Types._
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.tessellation.BuildInfo
import org.tessellation.currency.dataApplication._
import org.tessellation.currency.dataApplication.dataApplication._
import org.tessellation.currency.l1.CurrencyL1App
import org.tessellation.ext.cats.effect.ResourceIO
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.schema.semver.{MetagraphVersion, TessellationVersion}
import org.tessellation.security.hash.Hash
import org.tessellation.security.signature.Signed

import java.util.UUID

object Main
  extends CurrencyL1App(
    "currency-data_l1",
    "currency data L1 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version),
    tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version)
  ) {
  private def makeBaseDataApplicationL1Service(
    calculatedStateService: CalculatedStateService[IO]
  ): BaseDataApplicationL1Service[IO] = BaseDataApplicationL1Service(new DataApplicationL1Service[IO, NFTUpdate, NFTUpdatesState, NFTUpdatesCalculatedState] {
    override def validateData(
      state  : DataState[NFTUpdatesState, NFTUpdatesCalculatedState],
      updates: NonEmptyList[Signed[NFTUpdate]]
    )(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] =
      valid.pure[IO]

    override def validateUpdate(
      update: NFTUpdate
    )(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] =
      LifecycleSharedFunctions.validateUpdate[IO](update)

    override def combine(
      state  : DataState[NFTUpdatesState, NFTUpdatesCalculatedState],
      updates: List[Signed[NFTUpdate]]
    )(implicit context: L1NodeContext[IO]): IO[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]] =
      state.pure[IO]

    override def serializeState(
      state: NFTUpdatesState
    ): IO[Array[Byte]] =
      IO(Serializers.serializeState(state))

    override def serializeUpdate(
      update: NFTUpdate
    ): IO[Array[Byte]] =
      IO(Serializers.serializeUpdate(update))

    override def serializeBlock(
      block: Signed[DataApplicationBlock]
    ): IO[Array[Byte]] =
      IO(Serializers.serializeBlock(block)(dataEncoder.asInstanceOf[Encoder[DataUpdate]]))

    override def deserializeState(
      bytes: Array[Byte]
    ): IO[Either[Throwable, NFTUpdatesState]] =
      IO(Deserializers.deserializeState(bytes))

    override def deserializeUpdate(
      bytes: Array[Byte]
    ): IO[Either[Throwable, NFTUpdate]] =
      IO(Deserializers.deserializeUpdate(bytes))

    override def deserializeBlock(
      bytes: Array[Byte]
    ): IO[Either[Throwable, Signed[DataApplicationBlock]]] =
      IO(Deserializers.deserializeBlock(bytes)(dataDecoder.asInstanceOf[Decoder[DataUpdate]]))

    override def dataEncoder: Encoder[NFTUpdate] =
      implicitly[Encoder[NFTUpdate]]

    override def dataDecoder: Decoder[NFTUpdate] =
      implicitly[Decoder[NFTUpdate]]

    override def calculatedStateEncoder: Encoder[NFTUpdatesCalculatedState] =
      implicitly[Encoder[NFTUpdatesCalculatedState]]

    override def calculatedStateDecoder: Decoder[NFTUpdatesCalculatedState] =
      implicitly[Decoder[NFTUpdatesCalculatedState]]

    override def routes(implicit context: L1NodeContext[IO]): HttpRoutes[IO] =
      HttpRoutes.empty

    override def signedDataEntityDecoder: EntityDecoder[IO, Signed[NFTUpdate]] =
      circeEntityDecoder

    override def getCalculatedState(implicit context: L1NodeContext[IO]): IO[(SnapshotOrdinal, NFTUpdatesCalculatedState)] =
      calculatedStateService.getCalculatedState.map(calculatedState => (calculatedState.ordinal, calculatedState.state))

    override def setCalculatedState(
      ordinal: SnapshotOrdinal,
      state  : NFTUpdatesCalculatedState
    )(implicit context: L1NodeContext[IO]): IO[Boolean] =
      calculatedStateService.setCalculatedState(ordinal, state)

    override def hashCalculatedState(
      state: NFTUpdatesCalculatedState
    )(implicit context: L1NodeContext[IO]): IO[Hash] =
      calculatedStateService.hashCalculatedState(state)

    override def serializeCalculatedState(
      state: NFTUpdatesCalculatedState
    ): IO[Array[Byte]] =
      IO(Serializers.serializeCalculatedState(state))

    override def deserializeCalculatedState(
      bytes: Array[Byte]
    ): IO[Either[Throwable, NFTUpdatesCalculatedState]] =
      IO(Deserializers.deserializeCalculatedState(bytes))
  })

  private def makeL1Service: IO[BaseDataApplicationL1Service[IO]] =
    CalculatedStateService.make[IO].map(makeBaseDataApplicationL1Service)

  override def dataApplication: Option[Resource[IO, BaseDataApplicationL1Service[IO]]] =
    makeL1Service.asResource.some
}
