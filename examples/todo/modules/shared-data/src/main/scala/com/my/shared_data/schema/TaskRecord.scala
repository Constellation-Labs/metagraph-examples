package com.my.shared_data.schema

import cats.effect.Async
import cats.implicits.toFunctorOps

import com.my.shared_data.schema.Updates.CreateTask

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.{Decoder, Encoder}
import io.constellationnetwork.metagraph_sdk.std.JsonBinaryHasher.HasherOps
import io.constellationnetwork.schema.SnapshotOrdinal
import io.constellationnetwork.schema.address.Address

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

  def generateId[F[_]: Async](update: CreateTask): F[String] =
    update.computeDigest.map(_.value)

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
