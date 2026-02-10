package com.my.metagraph_l0

import cats.Applicative
import cats.data.NonEmptyList
import cats.effect.Async
import cats.syntax.all._

import io.constellationnetwork.currency.dataApplication._
import io.constellationnetwork.currency.dataApplication.dataApplication.{
  DataApplicationBlock,
  DataApplicationValidationErrorOr
}
import io.constellationnetwork.currency.schema.currency.CurrencyIncrementalSnapshot
import io.constellationnetwork.json.JsonSerializer
import io.constellationnetwork.schema.SnapshotOrdinal
import io.constellationnetwork.security.hash.Hash
import io.constellationnetwork.security.signature.Signed
import io.constellationnetwork.security.{Hashed, Hasher, SecurityProvider}

import com.my.metagraph_l0.ML0NodeContext.syntax.dataStateOps
import com.my.shared_data.lib.CirceOps.implicits._
import com.my.shared_data.lib.syntax.{CurrencyIncrementalSnapshotOps, ListSignedUpdateOps}
import com.my.shared_data.lib.{Checkpoint, CheckpointService, LatestUpdateValidator, SignedUpdateReducer}
import com.my.shared_data.lifecycle.StateUpdateCombiner
import com.my.shared_data.schema.Updates.TodoUpdate
import com.my.shared_data.schema.{CalculatedState, OnChain}

import io.circe.{Decoder, Encoder}
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.{EntityDecoder, HttpRoutes}
import org.typelevel.log4cats.Logger

object ML0Service {

  def make[F[+_]: Async: SecurityProvider: Hasher: JsonSerializer: Logger]: F[BaseDataApplicationL0Service[F]] = for {
    checkpointService <- CheckpointService.make[F, CalculatedState](CalculatedState.genesis)
    combiner = SignedUpdateReducer.make[F, TodoUpdate, DataState[OnChain, CalculatedState]](StateUpdateCombiner.make[F])
    validator = LatestUpdateValidator.make[F, Signed[TodoUpdate], DataState[OnChain, CalculatedState]](
      ML0Validator.make[F]
    )
    dataApplicationL0Service = makeBaseApplicationL0Service(checkpointService, combiner, validator)
  } yield dataApplicationL0Service

  private def makeBaseApplicationL0Service[F[+_]: Async: SecurityProvider: Hasher: JsonSerializer: Logger](
    checkpointService: CheckpointService[F, CalculatedState],
    combiner:          SignedUpdateReducer[F, TodoUpdate, DataState[OnChain, CalculatedState]],
    validator:         LatestUpdateValidator[F, Signed[TodoUpdate], DataState[OnChain, CalculatedState]]
  ): BaseDataApplicationL0Service[F] =
    BaseDataApplicationL0Service[F, TodoUpdate, OnChain, CalculatedState](
      new DataApplicationL0Service[F, TodoUpdate, OnChain, CalculatedState] {

        override def serializeState(state: OnChain): F[Array[Byte]] =
          JsonSerializer[F].serialize[OnChain](state)

        override def deserializeState(bytes: Array[Byte]): F[Either[Throwable, OnChain]] =
          JsonSerializer[F].deserialize[OnChain](bytes)

        override def serializeUpdate(update: TodoUpdate): F[Array[Byte]] =
          JsonSerializer[F].serialize[TodoUpdate](update)

        override def deserializeUpdate(bytes: Array[Byte]): F[Either[Throwable, TodoUpdate]] =
          JsonSerializer[F].deserialize[TodoUpdate](bytes)

        override def serializeBlock(block: Signed[DataApplicationBlock]): F[Array[Byte]] =
          JsonSerializer[F].serialize[Signed[DataApplicationBlock]](block)

        override def deserializeBlock(bytes: Array[Byte]): F[Either[Throwable, Signed[DataApplicationBlock]]] =
          JsonSerializer[F].deserialize[Signed[DataApplicationBlock]](bytes)

        override def serializeCalculatedState(calculatedState: CalculatedState): F[Array[Byte]] =
          JsonSerializer[F].serialize[CalculatedState](calculatedState)

        override def deserializeCalculatedState(bytes: Array[Byte]): F[Either[Throwable, CalculatedState]] =
          JsonSerializer[F].deserialize[CalculatedState](bytes)

        override def dataEncoder: Encoder[TodoUpdate] = implicitly(Encoder[TodoUpdate])

        override def dataDecoder: Decoder[TodoUpdate] = implicitly(Decoder[TodoUpdate])

        override def calculatedStateEncoder: Encoder[CalculatedState] = implicitly(Encoder[CalculatedState])

        override def calculatedStateDecoder: Decoder[CalculatedState] = implicitly(Decoder[CalculatedState])

        override val signedDataEntityDecoder: EntityDecoder[F, Signed[TodoUpdate]] = circeEntityDecoder

        override def getCalculatedState(implicit
          context: L0NodeContext[F]
        ): F[(SnapshotOrdinal, CalculatedState)] =
          checkpointService.get.map(checkpoint => (checkpoint.ordinal, checkpoint.state))

        override def setCalculatedState(ordinal: SnapshotOrdinal, state: CalculatedState)(implicit
          context: L0NodeContext[F]
        ): F[Boolean] = checkpointService.set(Checkpoint(ordinal, state))

        override def hashCalculatedState(state: CalculatedState)(implicit context: L0NodeContext[F]): F[Hash] =
          Hasher[F].hash(state)

        override def genesis: DataState[OnChain, CalculatedState] =
          DataState(OnChain.genesis, CalculatedState.genesis)

        override def onSnapshotConsensusResult(snapshot: Hashed[CurrencyIncrementalSnapshot])(implicit
          A: Applicative[F]
        ): F[Unit] =
          for {
            _               <- Logger[F].debug("Evaluating onSnapshotConsensusResult")
            numberOfUpdates <- snapshot.signed.value.countUpdates
            _ <- Logger[F].info(
              s"[onSnapshotConsensusResult] Got $numberOfUpdates updates for ordinal: ${snapshot.ordinal.value}"
            )
          } yield ()

        override def validateData(
          state:   DataState[OnChain, CalculatedState],
          updates: NonEmptyList[Signed[TodoUpdate]]
        )(implicit context: L0NodeContext[F]): F[DataApplicationValidationErrorOr[Unit]] =
          state.verify(updates)(validator)

        override def combine(
          state:   DataState[OnChain, CalculatedState],
          updates: List[Signed[TodoUpdate]]
        )(implicit context: L0NodeContext[F]): F[DataState[OnChain, CalculatedState]] =
          state.insert(updates.toSortedSet)(combiner)

        override def routes(implicit context: L0NodeContext[F]): HttpRoutes[F] =
          new ML0CustomRoutes[F](checkpointService).public
      }
    )
}
