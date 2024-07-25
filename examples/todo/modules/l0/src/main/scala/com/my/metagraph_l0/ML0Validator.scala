package com.my.metagraph_l0

import cats.effect.Async
import cats.implicits.{toFlatMapOps, toFoldableOps, toFunctorOps}
import cats.syntax.applicative._
import cats.syntax.validated._

import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataApplicationValidationError, DataState}
import org.tessellation.security.Hasher
import org.tessellation.security.signature.Signed

import com.my.shared_data.lib.UpdateValidator
import com.my.shared_data.lifecycle.ValidatorRules
import com.my.shared_data.schema.TaskRecord.generateId
import com.my.shared_data.schema.Updates.{CompleteTask, ModifyTask, RemoveTask, TodoUpdate}
import com.my.shared_data.schema.{CalculatedState, OnChain, Updates}

trait ML0Validator[F[_], U, T] extends UpdateValidator[F, U, T]

object ML0Validator {

  type TX = TodoUpdate
  type DS = DataState[OnChain, CalculatedState]

  def make[F[_]: Async: Hasher]: ML0Validator[F, Signed[TX], DS] =
    new ML0Validator[F, Signed[TX], DS] {

      override def verify(state: DS, signedUpdate: Signed[TX]): F[DataApplicationValidationErrorOr[Unit]] =
        signedUpdate.value match {
          case u: Updates.CreateTask   => createTask(u, state.onChain)
          case u: Updates.ModifyTask   => modifyTask(u, state.onChain)
          case u: Updates.CompleteTask => completeTask(u, state.onChain)
          case u: Updates.RemoveTask   => removeTask(u, state.onChain)
        }

      private def createTask(
        update: Updates.CreateTask,
        state: OnChain
      ): F[DataApplicationValidationErrorOr[Unit]] =
        for {
          id   <- generateId(update)
          res1 <- ValidatorRules.dueDateInFuture(BigInt(update.dueDate).longValue)
          res2 = ValidatorRules.taskDoesNotExist(id, state)
        } yield List(res1, res2).combineAll

      private def modifyTask(
        update: ModifyTask,
        state: OnChain
      ): F[DataApplicationValidationErrorOr[Unit]] =
        for {
          res1 <- update.optDueDate match {
            case Some(newDate) => ValidatorRules.dueDateInFuture(BigInt(newDate).longValue)
            case None          => ().validNec[DataApplicationValidationError].pure[F]
          }
          res2 = ValidatorRules.taskDoesExist(update.id, state)
          res3 = ValidatorRules.hasValidStatus(update)
        } yield List(res1, res2, res3).combineAll

      private def completeTask(
        update: CompleteTask,
        state: OnChain
      ): F[DataApplicationValidationErrorOr[Unit]] =
        List(
          ValidatorRules.taskDoesExist(update.id, state)
        ).combineAll.pure[F]

      private def removeTask(
        update: RemoveTask,
        state: OnChain
      ): F[DataApplicationValidationErrorOr[Unit]] =
        List(
          ValidatorRules.taskDoesExist(update.id, state)
        ).combineAll.pure[F]
    }
}
