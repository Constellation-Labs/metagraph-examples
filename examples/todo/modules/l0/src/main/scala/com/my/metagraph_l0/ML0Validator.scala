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
import com.my.shared_data.schema.Updates.{TodoUpdate}
import com.my.shared_data.schema.{CalculatedState, OnChain, Updates}

trait ML0Validator[F[_], U, T] extends UpdateValidator[F, U, T]

object ML0Validator {

  type TX = TodoUpdate
  type DS = DataState[OnChain, CalculatedState]

  def make[F[_] : Async : Hasher]: ML0Validator[F, Signed[TX], DS] =
    new ML0Validator[F, Signed[TX], DS] {

      override def verify(state: DS, signedUpdate: Signed[TX]): F[DataApplicationValidationErrorOr[Unit]] = ???
    }
}
