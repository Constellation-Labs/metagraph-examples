package com.my.metagraph_l0

import cats.effect.Async
import cats.syntax.all._

import org.tessellation.currency.dataApplication.{DataApplicationValidationError, L0NodeContext}
import org.tessellation.json.JsonSerializer
import org.tessellation.node.shared.ext.http4s.SnapshotOrdinalVar

import com.my.metagraph_l0.ML0NodeContext.syntax._
import com.my.shared_data.lib.{CheckpointService, MetagraphPublicRoutes}
import com.my.shared_data.schema.CalculatedState

import org.http4s.HttpRoutes

class ML0CustomRoutes[F[_]: Async: JsonSerializer](
  implicit context: L0NodeContext[F]
) extends MetagraphPublicRoutes[F] {

  protected val routes: HttpRoutes[F] = HttpRoutes.empty
}
