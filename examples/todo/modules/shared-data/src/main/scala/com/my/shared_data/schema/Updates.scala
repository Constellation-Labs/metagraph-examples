package com.my.shared_data.schema

import org.tessellation.currency.dataApplication.DataUpdate

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

object Updates {

  @derive(decoder, encoder)
  case class TodoUpdate() extends DataUpdate
}
