package com.my.shared_data.lifecycle

import cats.effect.Async

import org.tessellation.currency.dataApplication.DataState
import org.tessellation.security.SecurityProvider
import org.tessellation.security.signature.Signed

import com.my.shared_data.schema.Updates.TodoUpdate
import com.my.shared_data.schema.{CalculatedState, OnChain, Updates}

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
          case u: Updates.CreateTask   => ???
          case u: Updates.ModifyTask   => ???
          case u: Updates.CompleteTask => ???
          case u: Updates.RemoveTask   => ???
        }
    }
}
