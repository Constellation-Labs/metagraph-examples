package com.my.shared_data.schema

import org.tessellation.currency.dataApplication.DataOnChainState

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

@derive(decoder, encoder)
final case class OnChain(
  activeTasks: Map[String, TaskRecord]
) extends DataOnChainState

object OnChain {
  val genesis: OnChain = OnChain(Map.empty)
}
