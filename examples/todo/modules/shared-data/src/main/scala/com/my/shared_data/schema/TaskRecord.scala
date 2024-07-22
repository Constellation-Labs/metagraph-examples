package com.my.shared_data.schema

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

@derive(decoder, encoder)
final case class TaskRecord(
  id:                       String,
  creationDateTimestamp:    Long,
  lastUpdatedDateTimestamp: Long
)
