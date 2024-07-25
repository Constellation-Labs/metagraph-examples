package com.my.shared_data.schema

import org.tessellation.currency.dataApplication.DataCalculatedState

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

@derive(decoder, encoder)
final case class CalculatedState() extends DataCalculatedState

object CalculatedState {
  def genesis: CalculatedState = ???
}
