package com.my.shared_data.schema

import java.nio.charset.StandardCharsets

import cats.effect.Async
import cats.implicits.toFunctorOps

import com.my.shared_data.schema.Updates.CreateTask

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3
import org.bouncycastle.util.encoders.Hex

@derive(decoder, encoder)
final case class TaskRecord(
  id:                       String,
  creationDateTimestamp:    Long,
  lastUpdatedDateTimestamp: Long,
  dueDateTimestamp:         Long,
  status:                   TaskStatus
)

object TaskRecord {

  def generateId[F[_]: Async](update: CreateTask): F[String] =
    Async[F]
      .delay(
        update.description.getBytes(StandardCharsets.UTF_8) ++
        BigInt.long2bigInt(update.dueDate).toByteArray
      )
      .map { msg =>
        Hex.toHexString(new DigestSHA3(256).digest(msg))
      }
}

@derive(decoder, encoder)
sealed trait TaskStatus

@derive(decoder, encoder)
object TaskStatus {
  case object Backlog extends TaskStatus
  case object InProgress extends TaskStatus
  case object InReview extends TaskStatus
  case object Closed extends TaskStatus
}
