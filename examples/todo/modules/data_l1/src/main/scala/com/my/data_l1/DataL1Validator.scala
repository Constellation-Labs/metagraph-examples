package com.my.data_l1

import cats.effect.Async
import cats.implicits.{toFlatMapOps, toFoldableOps, toFunctorOps}
import cats.syntax.applicative._
import cats.syntax.validated._

import org.tessellation.currency.dataApplication.DataApplicationValidationError
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr

import com.my.shared_data.lib.UpdateValidator
import com.my.shared_data.lifecycle.ValidatorRules
import com.my.shared_data.schema.TaskRecord.generateId
import com.my.shared_data.schema.Updates.{CompleteTask, ModifyTask, RemoveTask, TodoUpdate}
import com.my.shared_data.schema.{OnChain, Updates}

trait DataL1Validator[F[_], U, T] extends UpdateValidator[F, U, T]

object DataL1Validator {

  def make[F[_]: Async]: DataL1Validator[F, TodoUpdate, OnChain] =
    new DataL1Validator[F, TodoUpdate, OnChain] {

      override def verify(state: OnChain, update: TodoUpdate): F[DataApplicationValidationErrorOr[Unit]] =
        update match {
          case u: Updates.CreateTask   => createTask(u)(state)
          case u: Updates.ModifyTask   => modifyTask(u)(state)
          case u: Updates.CompleteTask => completeTask(u)(state)
          case u: Updates.RemoveTask   => removeTask(u)(state)
        }

      private def createTask(
        update: Updates.CreateTask
      ): OnChain => F[DataApplicationValidationErrorOr[Unit]] = (state: OnChain) =>
        for {
          id   <- generateId(update)
          res1 <- ValidatorRules.dueDateInFuture(update.dueDate)
          res2 = ValidatorRules.taskDoesNotExist(id, state)
        } yield List(res1, res2).combineAll

      private def modifyTask(
        update: ModifyTask
      ): OnChain => F[DataApplicationValidationErrorOr[Unit]] = (state: OnChain) =>
        (update.optDueDate match {
          case Some(newDate) => ValidatorRules.dueDateInFuture(newDate)
          case None          => ().validNec[DataApplicationValidationError].pure[F]
        }).map { res1 =>
          List(
            res1,
            ValidatorRules.taskDoesExist(update.id, state)
          ).combineAll
        }

      private def completeTask(
        update: CompleteTask
      ): OnChain => F[DataApplicationValidationErrorOr[Unit]] = (state: OnChain) =>
        List(
          ValidatorRules.taskDoesExist(update.id, state)
        ).combineAll.pure[F]

      private def removeTask(
        update: RemoveTask
      ): OnChain => F[DataApplicationValidationErrorOr[Unit]] = (state: OnChain) =>
        List(
          ValidatorRules.taskDoesExist(update.id, state)
        ).combineAll.pure[F]
    }
}
