package com.my.voting_poll.shared_data

import cats.Functor
import cats.syntax.functor._
import org.tessellation.currency.dataApplication.{L0NodeContext, L1NodeContext}
import org.tessellation.currency.schema.currency.CurrencySnapshotInfo
import org.tessellation.schema.SnapshotOrdinal


object Utils {

  def getLastMetagraphIncrementalSnapshotInfo[F[_] : Functor](context: Either[L0NodeContext[F], L1NodeContext[F]]): F[Option[CurrencySnapshotInfo]] = {
    context
      .fold(_.getLastCurrencySnapshotCombined, _.getLastCurrencySnapshotCombined)
      .map(_.map(_._2))
  }

  def getLastCurrencySnapshotOrdinal[F[_]: Functor](context: Either[L0NodeContext[F], L1NodeContext[F]]): F[Option[SnapshotOrdinal]] = {
    context match {
      case Left(l0Context) =>
        l0Context.getLastCurrencySnapshot.map(_.map(_.ordinal))
      case Right(l1Context) =>
        l1Context.getLastCurrencySnapshot.map(_.map(_.ordinal))
    }
  }
}

