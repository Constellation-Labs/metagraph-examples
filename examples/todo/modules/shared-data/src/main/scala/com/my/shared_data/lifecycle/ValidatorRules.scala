package com.my.shared_data.lifecycle

import cats.data.Validated
import cats.effect.{Async, Clock}
import cats.implicits.toFunctorOps
import cats.syntax.validated._

import scala.concurrent.duration.DurationInt

import io.constellationnetwork.currency.dataApplication.DataApplicationValidationError
import io.constellationnetwork.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr

import com.my.shared_data.schema.Updates.ModifyTask
import com.my.shared_data.schema.{OnChain, TaskStatus}

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

object ValidatorRules {

  def dueDateInFuture[F[_]: Async](
    dueDate: Long
  ): F[DataApplicationValidationErrorOr[Unit]] =
    Clock[F].realTime.map { now =>
      Validated.condNec((now + 1.hours).toMillis < dueDate, (), Errors.TaskDueDateInPast)
    }

  def taskDoesNotExist(
    id:    String,
    state: OnChain
  ): DataApplicationValidationErrorOr[Unit] =
    Validated.condNec(!state.activeTasks.contains(id), (), Errors.RecordAlreadyExists)

  def taskDoesExist(
    id:    String,
    state: OnChain
  ): DataApplicationValidationErrorOr[Unit] =
    Validated.condNec(state.activeTasks.contains(id), (), Errors.RecordDoesNotExist)

  def hasValidStatus(
    update: ModifyTask
  ): DataApplicationValidationErrorOr[Unit] =
    update.optStatus match {
      case Some(status) =>
        status match {
          case TaskStatus.Complete => Errors.InvalidStatusForModify.invalidNec
          case TaskStatus.Closed   => Errors.InvalidStatusForModify.invalidNec
          case _                   => ().validNec
        }
      case None => ().validNec
    }

  object Errors {

    @derive(decoder, encoder)
    case object RecordAlreadyExists extends DataApplicationValidationError {
      val message = s"Failed to create task, previous record found."
    }

    @derive(decoder, encoder)
    case object RecordDoesNotExist extends DataApplicationValidationError {
      val message = s"Failed to create event, no previous record found."
    }

    @derive(decoder, encoder)
    case object TaskDueDateInPast extends DataApplicationValidationError {
      val message = s"Task due date must be at least 1 hour in the future."
    }

    @derive(decoder, encoder)
    case object InvalidStatusForModify extends DataApplicationValidationError {
      val message = s"Invalid status found in ModifyTask. Use RemoveTask to archive."
    }
  }
}
