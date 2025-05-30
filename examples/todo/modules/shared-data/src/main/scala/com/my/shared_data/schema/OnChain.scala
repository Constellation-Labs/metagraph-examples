package com.my.shared_data.schema

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.constellationnetwork.currency.dataApplication.DataOnChainState

@derive(decoder, encoder)
final case class OnChain(
  activeTasks: Map[String, TaskRecord]
) extends DataOnChainState

object OnChain {
  val genesis: OnChain = OnChain(Map.empty)
}
