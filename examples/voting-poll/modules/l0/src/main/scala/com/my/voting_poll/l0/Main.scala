package com.my.voting_poll.l0

import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import cats.syntax.option.catsSyntaxOptionId
import com.my.voting_poll.l0.custom_routes.CustomRoutes
import com.my.voting_poll.shared_data.LifecycleSharedFunctions
import com.my.voting_poll.shared_data.calculated_state.CalculatedStateService
import com.my.voting_poll.shared_data.deserializers.Deserializers
import com.my.voting_poll.shared_data.serializers.Serializers
import com.my.voting_poll.shared_data.types.Types._
import io.circe.{Decoder, Encoder}
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.{EntityDecoder, HttpRoutes}
import io.constellationnetwork.BuildInfo
import io.constellationnetwork.currency.dataApplication._
import io.constellationnetwork.currency.dataApplication.dataApplication.{DataApplicationBlock, DataApplicationValidationErrorOr}
import io.constellationnetwork.currency.l0.CurrencyL0App
import io.constellationnetwork.ext.cats.effect.ResourceIO
import io.constellationnetwork.schema.SnapshotOrdinal
import io.constellationnetwork.schema.cluster.ClusterId
import io.constellationnetwork.schema.semver.{MetagraphVersion, TessellationVersion}
import io.constellationnetwork.security.hash.Hash
import io.constellationnetwork.security.signature.Signed

import java.util.UUID

object Main extends CurrencyL0App(
  "currency-l0",
  "currency L0 node",
  ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
  metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version),
  tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version)
) {

  private def makeBaseDataApplicationL0Service(
    calculatedStateService: CalculatedStateService[IO]
  ): BaseDataApplicationL0Service[IO] =
    BaseDataApplicationL0Service(new DataApplicationL0Service[IO, PollUpdate, VoteStateOnChain, VoteCalculatedState] {
      override def genesis: DataState[VoteStateOnChain, VoteCalculatedState] = DataState(VoteStateOnChain(List.empty), VoteCalculatedState(Map.empty))

      override def validateData(state: DataState[VoteStateOnChain, VoteCalculatedState], updates: NonEmptyList[Signed[PollUpdate]])(implicit context: L0NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = LifecycleSharedFunctions.validateData[IO](state, updates)

      override def combine(state: DataState[VoteStateOnChain, VoteCalculatedState], updates: List[Signed[PollUpdate]])(implicit context: L0NodeContext[IO]): IO[DataState[VoteStateOnChain, VoteCalculatedState]] = LifecycleSharedFunctions.combine[IO](state, updates)

      override def serializeState(state: VoteStateOnChain): IO[Array[Byte]] = IO(Serializers.serializeState(state))

      override def serializeUpdate(update: PollUpdate): IO[Array[Byte]] = IO(Serializers.serializeUpdate(update))

      override def serializeBlock(block: Signed[DataApplicationBlock]): IO[Array[Byte]] = IO(Serializers.serializeBlock(block)(dataEncoder.asInstanceOf[Encoder[DataUpdate]]))

      override def deserializeState(bytes: Array[Byte]): IO[Either[Throwable, VoteStateOnChain]] = IO(Deserializers.deserializeState(bytes))

      override def deserializeUpdate(bytes: Array[Byte]): IO[Either[Throwable, PollUpdate]] = IO(Deserializers.deserializeUpdate(bytes))

      override def deserializeBlock(bytes: Array[Byte]): IO[Either[Throwable, Signed[DataApplicationBlock]]] = IO(Deserializers.deserializeBlock(bytes)(dataDecoder.asInstanceOf[Decoder[DataUpdate]]))

      override def dataEncoder: Encoder[PollUpdate] = implicitly[Encoder[PollUpdate]]

      override def dataDecoder: Decoder[PollUpdate] = implicitly[Decoder[PollUpdate]]

      override def routes(implicit context: L0NodeContext[IO]): HttpRoutes[IO] = CustomRoutes[IO](calculatedStateService).public

      override def signedDataEntityDecoder: EntityDecoder[IO, Signed[PollUpdate]] = circeEntityDecoder

      override def calculatedStateEncoder: Encoder[VoteCalculatedState] = implicitly[Encoder[VoteCalculatedState]]

      override def calculatedStateDecoder: Decoder[VoteCalculatedState] = implicitly[Decoder[VoteCalculatedState]]

      override def getCalculatedState(implicit context: L0NodeContext[IO]): IO[(SnapshotOrdinal, VoteCalculatedState)] = calculatedStateService.getCalculatedState.map(calculatedState => (calculatedState.ordinal, calculatedState.state))

      override def setCalculatedState(ordinal: SnapshotOrdinal, state: VoteCalculatedState)(implicit context: L0NodeContext[IO]): IO[Boolean] = calculatedStateService.setCalculatedState(ordinal, state)

      override def hashCalculatedState(state: VoteCalculatedState)(implicit context: L0NodeContext[IO]): IO[Hash] = calculatedStateService.hashCalculatedState(state)

      override def serializeCalculatedState(state: VoteCalculatedState): IO[Array[Byte]] = IO(Serializers.serializeCalculatedState(state))

      override def deserializeCalculatedState(bytes: Array[Byte]): IO[Either[Throwable, VoteCalculatedState]] = IO(Deserializers.deserializeCalculatedState(bytes))
    })

  private def makeL0Service: IO[BaseDataApplicationL0Service[IO]] = {
    for {
      calculatedStateService <- CalculatedStateService.make[IO]
      dataApplicationL0Service = makeBaseDataApplicationL0Service(calculatedStateService)
    } yield dataApplicationL0Service
  }

  override def dataApplication: Option[Resource[IO, BaseDataApplicationL0Service[IO]]] =
    makeL0Service.asResource.some
}
