package com.my.shared_data.lib

import cats.data.NonEmptyList
import cats.effect.Async
import cats.implicits.toFunctorOps

import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr

import com.my.shared_data.lib.UpdateValidator

trait LatestUpdateValidator[F[_], U, T] {

  def checkAll(latest: T, batch: NonEmptyList[U]): F[DataApplicationValidationErrorOr[Unit]]
}

object LatestUpdateValidator {

  def make[F[_]: Async, U, T](
    updateValidator: UpdateValidator[F, U, T]
  ): LatestUpdateValidator[F, U, T] =
    (latest: T, batch: NonEmptyList[U]) =>
      batch
        .traverse(u => updateValidator.verify(latest, u))
        .map(_.reduce)
}
