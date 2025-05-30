package com.my.metagraph_l0

import cats.Applicative
import cats.data.NonEmptyList
import cats.effect.Async
import cats.syntax.all._
import com.my.metagraph_l0.ML0NodeContext.syntax.dataStateOps
import com.my.shared_data.lib.syntax.ListSignedUpdateOps
import com.my.shared_data.lib.{LatestUpdateValidator, SignedUpdateReducer}
import com.my.shared_data.lifecycle.StateUpdateCombiner
import com.my.shared_data.schema.Updates.TodoUpdate
import com.my.shared_data.schema.{CalculatedState, OnChain}
import io.constellationnetwork.currency.dataApplication._
import io.constellationnetwork.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import io.constellationnetwork.currency.schema.currency.CurrencyIncrementalSnapshot
import io.constellationnetwork.metagraph_sdk.MetagraphCommonService
import io.constellationnetwork.metagraph_sdk.lifecycle.CheckpointService
import io.constellationnetwork.metagraph_sdk.std.Checkpoint
import io.constellationnetwork.metagraph_sdk.std.JsonBinaryHasher.HasherOps
import io.constellationnetwork.metagraph_sdk.syntax.all.CurrencyIncrementalSnapshotOps
import io.constellationnetwork.schema.SnapshotOrdinal
import io.constellationnetwork.security.hash.Hash
import io.constellationnetwork.security.signature.Signed
import io.constellationnetwork.security.{Hashed, SecurityProvider}
import org.http4s.HttpRoutes
import org.typelevel.log4cats.Logger

object ML0Service {

  def make[F[+_]: Async: SecurityProvider: Logger]: F[BaseDataApplicationL0Service[F]] = for {
    checkpointService <- CheckpointService.make[F, CalculatedState](CalculatedState.genesis)
    combiner = SignedUpdateReducer.make[F, TodoUpdate, DataState[OnChain, CalculatedState]](StateUpdateCombiner.make[F])
    validator = LatestUpdateValidator.make[F, Signed[TodoUpdate], DataState[OnChain, CalculatedState]](
      ML0Validator.make[F]
    )
    dataApplicationL0Service = makeBaseApplicationL0Service(checkpointService, combiner, validator)
  } yield dataApplicationL0Service

  private def makeBaseApplicationL0Service[F[+_]: Async: SecurityProvider: Logger](
    checkpointService: CheckpointService[F, CalculatedState],
    combiner:          SignedUpdateReducer[F, TodoUpdate, DataState[OnChain, CalculatedState]],
    validator:         LatestUpdateValidator[F, Signed[TodoUpdate], DataState[OnChain, CalculatedState]]
  ): BaseDataApplicationL0Service[F] =
    BaseDataApplicationL0Service[F, TodoUpdate, OnChain, CalculatedState](
      new MetagraphCommonService[F, TodoUpdate, OnChain, CalculatedState, L0NodeContext[F]]
        with DataApplicationL0Service[F, TodoUpdate, OnChain, CalculatedState] {

        override def getCalculatedState(implicit
          context: L0NodeContext[F]
        ): F[(SnapshotOrdinal, CalculatedState)] =
          checkpointService.get.map(checkpoint => (checkpoint.ordinal, checkpoint.state))

        override def setCalculatedState(ordinal: SnapshotOrdinal, state: CalculatedState)(implicit
          context: L0NodeContext[F]
        ): F[Boolean] = checkpointService.set(Checkpoint(ordinal, state))

        override def hashCalculatedState(state: CalculatedState)(implicit context: L0NodeContext[F]): F[Hash] =
          state.computeDigest

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
