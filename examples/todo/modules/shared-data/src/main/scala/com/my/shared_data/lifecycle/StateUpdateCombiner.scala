package com.my.shared_data.lifecycle

import cats.effect.Async
import cats.implicits.{toFlatMapOps, toFunctorOps}

import com.my.shared_data.schema.TaskRecord.generateId
import com.my.shared_data.schema.Updates.TodoUpdate
import com.my.shared_data.schema._

import io.constellationnetwork.currency.dataApplication.{DataState, L0NodeContext}
import io.constellationnetwork.ext.cats.syntax.next.catsSyntaxNext
import io.constellationnetwork.schema.SnapshotOrdinal
import io.constellationnetwork.security.SecurityProvider
import io.constellationnetwork.security.signature.Signed
import monocle.Monocle.toAppliedFocusOps

trait StateUpdateCombiner[F[_], U, T] {
  def insert(state: T, signedUpdate: Signed[U])(implicit ctx: L0NodeContext[F]): F[T]
}

object StateUpdateCombiner {

  type TX = TodoUpdate
  type DS = DataState[OnChain, CalculatedState]

  def make[F[_]: Async: SecurityProvider]: StateUpdateCombiner[F, TX, DS] =
    new StateUpdateCombiner[F, TX, DS] {

      override def insert(state: DS, signedUpdate: Signed[TX])(implicit ctx: L0NodeContext[F]): F[DS] =
        signedUpdate.value match {
          case u: Updates.CreateTask   => createTask(Signed(u, signedUpdate.proofs))(state, ctx)
          case u: Updates.ModifyTask   => modifyTask(Signed(u, signedUpdate.proofs))(state, ctx)
          case u: Updates.CompleteTask => completeTask(Signed(u, signedUpdate.proofs))(state, ctx)
          case u: Updates.RemoveTask   => removeTask(Signed(u, signedUpdate.proofs))(state, ctx)
        }

      private def createTask(update: Signed[Updates.CreateTask]): (DS, L0NodeContext[F]) => F[DS] =
        (inState: DS, ctx: L0NodeContext[F]) =>
          for {
            currentOrdinal <- ctx.getLastCurrencySnapshot
              .map(_.map(_.signed.value.ordinal).getOrElse(SnapshotOrdinal.MinValue))
              .map(_.next)
            reporter <- update.proofs.head.id.toAddress[F]
            id       <- generateId(update)

            _record = TaskRecord(
              id,
              currentOrdinal,
              currentOrdinal,
              BigInt(update.dueDate).longValue,
              update.optStatus.getOrElse(TaskStatus.Backlog),
              reporter
            )

            onchain = inState.onChain
              .focus(_.activeTasks)
              .modify(_.updated(id, _record))

            calculated = inState.calculated

          } yield DataState(onchain, calculated)

      private def modifyTask(update: Signed[Updates.ModifyTask]): (DS, L0NodeContext[F]) => F[DS] =
        (inState: DS, ctx: L0NodeContext[F]) =>
          for {
            currentOrdinal <- ctx.getLastCurrencySnapshot
              .map(_.map(_.signed.value.ordinal).getOrElse(SnapshotOrdinal.MinValue))
              .map(_.next)

            prevRecord = inState.onChain.activeTasks(update.id)

            _record = prevRecord.copy(
              lastUpdatedOrdinal = currentOrdinal,
              status = update.optStatus.getOrElse(prevRecord.status),
              dueDateEpochMilli = update.optDueDate.map(BigInt(_).longValue).getOrElse(prevRecord.dueDateEpochMilli)
            )

            onchain = inState.onChain
              .focus(_.activeTasks)
              .modify(_.updated(update.id, _record))

            calculated = inState.calculated

          } yield DataState(onchain, calculated)

      private def completeTask(update: Signed[Updates.CompleteTask]): (DS, L0NodeContext[F]) => F[DS] =
        (inState: DS, ctx: L0NodeContext[F]) =>
          for {
            currentOrdinal <- ctx.getLastCurrencySnapshot
              .map(_.map(_.signed.value.ordinal).getOrElse(SnapshotOrdinal.MinValue))
              .map(_.next)

            prevRecord = inState.onChain.activeTasks(update.id)

            _record = prevRecord.copy(
              lastUpdatedOrdinal = currentOrdinal,
              status = TaskStatus.Complete
            )

            onchain = inState.onChain
              .focus(_.activeTasks)
              .modify(_.removed(update.id))

            calculated = inState.calculated
              .focus(_.history)
              .modify(_.updated(update.id, _record))

          } yield DataState(onchain, calculated)

      private def removeTask(update: Signed[Updates.RemoveTask]): (DS, L0NodeContext[F]) => F[DS] =
        (inState: DS, ctx: L0NodeContext[F]) =>
          for {
            currentOrdinal <- ctx.getLastCurrencySnapshot
              .map(_.map(_.signed.value.ordinal).getOrElse(SnapshotOrdinal.MinValue))
              .map(_.next)

            prevRecord = inState.onChain.activeTasks(update.id)

            _record = prevRecord.copy(
              lastUpdatedOrdinal = currentOrdinal,
              status = TaskStatus.Closed
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
