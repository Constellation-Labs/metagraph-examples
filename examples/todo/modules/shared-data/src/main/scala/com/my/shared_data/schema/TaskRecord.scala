package com.my.shared_data.schema

import cats.effect.Async
import cats.implicits.toFunctorOps

import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.address.Address
import org.tessellation.security.Hasher

import com.my.shared_data.schema.Updates.CreateTask

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.{Decoder, Encoder}

@derive(decoder, encoder)
final case class TaskRecord(
  id:                 String,
  creationOrdinal:    SnapshotOrdinal,
  lastUpdatedOrdinal: SnapshotOrdinal,
  dueDateEpochMilli:  Long,
  status:             TaskStatus,
  reporter:           Address
)

object TaskRecord {

  def generateId[F[_]: Async: Hasher](update: CreateTask): F[String] =
    Hasher[F].hash(update).map(_.value)

}

sealed trait TaskStatus

object TaskStatus {
  case object Backlog extends TaskStatus
  case object InProgress extends TaskStatus
  case object InReview extends TaskStatus
  case object Complete extends TaskStatus
  case object Closed extends TaskStatus

  implicit val config: Configuration = Configuration.default.withDiscriminator("type")

  implicit val decoder: Decoder[TaskStatus] = deriveConfiguredDecoder
  implicit val encoder: Encoder[TaskStatus] = deriveConfiguredEncoder
}
