package com.my.shared_data.lib

import cats.effect.Async
import cats.implicits.toFoldableOps

import scala.collection.immutable.SortedSet

import org.tessellation.security.signature.Signed

import com.my.shared_data.lifecycle.StateUpdateCombiner

trait SignedUpdateReducer[F[_], U, T] {
  def foldLeft(previous: T, batch: SortedSet[Signed[U]]): F[T]
}

object SignedUpdateReducer {

  def make[F[_]: Async, U, T](
    stateUpdateCombiner: StateUpdateCombiner[F, U, T]
  ): SignedUpdateReducer[F, U, T] =
    (previous: T, batch: SortedSet[Signed[U]]) =>
      batch.foldLeftM(previous) { (acc, update) =>
        stateUpdateCombiner.insert(acc, update)
      }
}
