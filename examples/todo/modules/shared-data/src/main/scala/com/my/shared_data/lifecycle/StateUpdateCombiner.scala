package com.my.shared_data.lifecycle

import cats.effect.Async
import cats.implicits.{toFlatMapOps, toFunctorOps}

import org.tessellation.currency.dataApplication.{DataState, L0NodeContext}
import org.tessellation.ext.cats.syntax.next.catsSyntaxNext
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.signature.Signed
import org.tessellation.security.{Hasher, SecurityProvider}

import com.my.shared_data.schema.Updates.TodoUpdate
import com.my.shared_data.schema._

import monocle.Monocle.toAppliedFocusOps

trait StateUpdateCombiner[F[_], U, T] {
  def insert(state: T, signedUpdate: Signed[U])(implicit ctx: L0NodeContext[F]): F[T]
}

object StateUpdateCombiner {

  type TX = TodoUpdate
  type DS = DataState[OnChain, CalculatedState]

  def make[F[_]: Async: SecurityProvider: Hasher]: StateUpdateCombiner[F, TX, DS] =
    new StateUpdateCombiner[F, TX, DS] {

      override def insert(state: DS, signedUpdate: Signed[TX])(implicit ctx: L0NodeContext[F]): F[DS] = ???

    }
}
