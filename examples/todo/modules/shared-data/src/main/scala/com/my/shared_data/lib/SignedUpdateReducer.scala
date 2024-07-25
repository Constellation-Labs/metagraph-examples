package com.my.shared_data.lib

import cats.effect.Async
import cats.implicits.toFoldableOps

import scala.collection.immutable.SortedSet

import org.tessellation.currency.dataApplication.L0NodeContext
import org.tessellation.security.signature.Signed

import com.my.shared_data.lifecycle.StateUpdateCombiner

trait SignedUpdateReducer[F[_], U, T] {
  def foldLeft(previous: T, batch: SortedSet[Signed[U]])(implicit ctx: L0NodeContext[F]): F[T]
}

object SignedUpdateReducer {

  def make[F[_]: Async, U, T](
    stateUpdateCombiner: StateUpdateCombiner[F, U, T]
  ): SignedUpdateReducer[F, U, T] =
    new SignedUpdateReducer[F, U, T] {
      override def foldLeft(previous: T, batch: SortedSet[Signed[U]])(implicit ctx: L0NodeContext[F]): F[T] =
        batch.foldLeftM(previous) { (acc, update) =>
          stateUpdateCombiner.insert(acc, update)
        }
    }
}
