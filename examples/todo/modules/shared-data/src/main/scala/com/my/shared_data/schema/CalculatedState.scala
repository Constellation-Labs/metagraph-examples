package com.my.shared_data.schema

import io.constellationnetwork.currency.dataApplication.DataCalculatedState

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

@derive(decoder, encoder)
final case class CalculatedState(
  history: Map[String, TaskRecord]
) extends DataCalculatedState

object CalculatedState {
  val genesis: CalculatedState = CalculatedState(Map.empty)
}
