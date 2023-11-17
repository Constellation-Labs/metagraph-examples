package com.my.currency.shared_data

import cats.Functor
import cats.syntax.functor._
import org.tessellation.currency.dataApplication.{L0NodeContext, L1NodeContext}
import org.tessellation.currency.schema.currency.CurrencySnapshotInfo


object Utils {

  def getLastMetagraphIncrementalSnapshotInfo[F[_] : Functor](context: Either[L0NodeContext[F], L1NodeContext[F]]): F[Option[CurrencySnapshotInfo]] = {
    context
      .fold(_.getLastCurrencySnapshotCombined, _.getLastCurrencySnapshotCombined)
      .map(_.map(_._2))
  }
}

