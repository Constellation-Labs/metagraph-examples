package com.my.metagraph_l0

import cats.effect.Async
import cats.syntax.all._
import com.my.metagraph_l0.ML0NodeContext.syntax.ML0NodeContextOps
import com.my.shared_data.schema.{CalculatedState, OnChain}
import io.circe.{Decoder, Encoder}
import io.constellationnetwork.currency.dataApplication.{DataApplicationValidationError, DataUpdate, L0NodeContext}
import io.constellationnetwork.metagraph_sdk.MetagraphPublicRoutes
import io.constellationnetwork.metagraph_sdk.lifecycle.CheckpointService
import io.constellationnetwork.metagraph_sdk.syntax.all.L0ContextOps
import io.constellationnetwork.node.shared.ext.http4s.SnapshotOrdinalVar
import org.http4s.HttpRoutes

class ML0CustomRoutes[F[_]: Async](calculatedStateService: CheckpointService[F, CalculatedState])(implicit
  context: L0NodeContext[F],
  txenc:   Encoder[DataUpdate],
  txdec:   Decoder[DataUpdate]
) extends MetagraphPublicRoutes[F] {

  protected val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "active-tasks" / "all" =>
      context.getOnChainState[OnChain].map(_.map(_.activeTasks.toList)).flatMap(prepareResponse(_))

    case GET -> Root / "archive-tasks" / "all" =>
      calculatedStateService.get
        .map(_.state.history.toList.asRight[DataApplicationValidationError])
        .flatMap(prepareResponse(_))

    case GET -> Root / "snapshot" / "currency" / "latest" =>
      context.getLatestCurrencySnapshot.flatMap(prepareResponse(_))

    case GET -> Root / "snapshot" / "currency" / SnapshotOrdinalVar(ordinal) =>
      context.getCurrencySnapshotAt(ordinal).flatMap(prepareResponse(_))

    case GET -> Root / "snapshot" / "currency" / SnapshotOrdinalVar(ordinal) / "count-updates" =>
      context.countUpdatesInSnapshotAt(ordinal).flatMap(prepareResponse(_))
  }
}
