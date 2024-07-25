package com.my.data_l1

import cats.effect.Async
import cats.implicits.{toFlatMapOps, toFoldableOps, toFunctorOps}
import cats.syntax.applicative._
import cats.syntax.validated._

import org.tessellation.currency.dataApplication.DataApplicationValidationError
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.security.Hasher

import com.my.shared_data.lib.UpdateValidator
import com.my.shared_data.lifecycle.ValidatorRules
import com.my.shared_data.schema.Updates.{TodoUpdate}
import com.my.shared_data.schema.{OnChain, Updates}

trait DataL1Validator[F[_], U, T] extends UpdateValidator[F, U, T]

object DataL1Validator {

  def make[F[_]: Async: Hasher]: DataL1Validator[F, TodoUpdate, OnChain] =
    new DataL1Validator[F, TodoUpdate, OnChain] {

      override def verify(state: OnChain, update: TodoUpdate): F[DataApplicationValidationErrorOr[Unit]] = ???
    }
}
