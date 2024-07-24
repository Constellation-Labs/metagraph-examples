package com.my.shared_data.lifecycle

import cats.effect.{Async, Clock}
import cats.implicits.{toFlatMapOps, toFunctorOps}

import org.tessellation.currency.dataApplication.DataState
import org.tessellation.security.SecurityProvider
import org.tessellation.security.signature.Signed

import com.my.shared_data.schema.TaskRecord.generateId
import com.my.shared_data.schema.Updates.TodoUpdate
import com.my.shared_data.schema._

import monocle.Monocle.toAppliedFocusOps

trait StateUpdateCombiner[F[_], U, T] {
  def insert(state: T, signedUpdate: Signed[U]): F[T]
}

object StateUpdateCombiner {

  type TX = TodoUpdate
  type DS = DataState[OnChain, CalculatedState]

  def make[F[_]: Async: SecurityProvider]: StateUpdateCombiner[F, TX, DS] =
    new StateUpdateCombiner[F, TX, DS] {

      override def insert(state: DS, signedUpdate: Signed[TX]): F[DS] =
        signedUpdate.value match {
          case u: Updates.CreateTask   => createTask(Signed(u, signedUpdate.proofs))(state)
          case u: Updates.ModifyTask   => modifyTask(Signed(u, signedUpdate.proofs))(state)
          case u: Updates.CompleteTask => completeTask(Signed(u, signedUpdate.proofs))(state)
          case u: Updates.RemoveTask   => removeTask(Signed(u, signedUpdate.proofs))(state)
        }

      private def createTask(update: Signed[Updates.CreateTask]): DS => F[DS] =
        (inState: DS) =>
          for {
            nowInTime <- Clock[F].realTime
            reporter  <- update.proofs.head.id.toAddress[F]
            id        <- generateId(update)

            _record = TaskRecord(
              id,
              nowInTime.toMillis,
              nowInTime.toMillis,
              update.dueDate,
              update.optStatus.getOrElse(TaskStatus.Backlog),
              reporter
            )

            onchain = inState.onChain
              .focus(_.activeTasks)
              .modify(_.updated(id, _record))

            calculated = inState.calculated

          } yield DataState(onchain, calculated)

      private def modifyTask(update: Signed[Updates.ModifyTask]): DS => F[DS] =
        (inState: DS) =>
          for {
            nowInTime <- Clock[F].realTime

            prevRecord = inState.onChain.activeTasks(update.id)

            _record = prevRecord.copy(
              lastUpdatedDateTimestamp = nowInTime.toMillis,
              status = update.optStatus.getOrElse(prevRecord.status),
              dueDateTimestamp = update.optDueDate.getOrElse(prevRecord.dueDateTimestamp)
            )

            onchain = inState.onChain
              .focus(_.activeTasks)
              .modify(_.updated(update.id, _record))

            calculated = inState.calculated

          } yield DataState(onchain, calculated)

      private def completeTask(update: Signed[Updates.CompleteTask]): DS => F[DS] =
        (inState: DS) =>
          for {
            nowInTime <- Clock[F].realTime

            prevRecord = inState.onChain.activeTasks(update.id)

            _record = prevRecord.copy(
              lastUpdatedDateTimestamp = nowInTime.toMillis,
              status = TaskStatus.Closed
            )

            onchain = inState.onChain
              .focus(_.activeTasks)
              .modify(_.removed(update.id))

            calculated = inState.calculated
              .focus(_.history)
              .modify(_.updated(update.id, _record))

          } yield DataState(onchain, calculated)

      private def removeTask(update: Signed[Updates.RemoveTask]): DS => F[DS] =
        (inState: DS) =>
          for {
            nowInTime <- Clock[F].realTime

            prevRecord = inState.onChain.activeTasks(update.id)

            _record = prevRecord.copy(
              lastUpdatedDateTimestamp = nowInTime.toMillis
            )

            onchain = inState.onChain
              .focus(_.activeTasks)
              .modify(_.removed(update.id))

            calculated = inState.calculated
              .focus(_.history)
              .modify(_.updated(update.id, _record))

          } yield DataState(onchain, calculated)
    }
}
