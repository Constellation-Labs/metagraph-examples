package com.my.metagraph_l0

import cats.effect.Async
import cats.syntax.all._

import org.tessellation.currency.dataApplication.L0NodeContext
import org.tessellation.json.JsonSerializer
import org.tessellation.node.shared.ext.http4s.SnapshotOrdinalVar

import com.my.metagraph_l0.ML0NodeContext.syntax._
import com.my.shared_data.MetagraphPublicRoutes

import org.http4s.HttpRoutes
import scalapb_circe.codec._

class ML0CustomRoutes[F[_]: Async: JsonSerializer](implicit context: L0NodeContext[F])
    extends MetagraphPublicRoutes[F] {

  protected val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "snapshot" / "currency" / "latest" =>
      context.getLatestCurrencySnapshot.flatMap(prepareResponse(_))

    case GET -> Root / "snapshot" / "currency" / SnapshotOrdinalVar(ordinal) =>
      context.getCurrencySnapshotAt(ordinal).flatMap(prepareResponse(_))

    case GET -> Root / "snapshot" / "currency" / SnapshotOrdinalVar(ordinal) / "count-updates" =>
      context.countUpdatesInSnapshotAt(ordinal).flatMap(prepareResponse(_))
  }
}
