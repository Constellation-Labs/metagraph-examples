package com.my.currency.shared_data

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.tessellation.currency.dataApplication.{L0NodeContext, L1NodeContext}
import org.tessellation.currency.schema.currency.{CurrencyIncrementalSnapshot, CurrencySnapshotInfo}
import org.tessellation.security.Hashed


object Utils {
  private def getSnapshotInfo(snapshotIO: IO[Option[(Hashed[CurrencyIncrementalSnapshot], CurrencySnapshotInfo)]]): Option[CurrencySnapshotInfo] = {
    snapshotIO.map {
      case Some(value) => Some(value._2)
      case None => None
    }.unsafeRunSync()
  }

  def getLastMetagraphIncrementalSnapshotInfo(context: Either[L0NodeContext[IO], L1NodeContext[IO]]): Option[CurrencySnapshotInfo] = {
    context match {
      case Left(value) =>
        val lastSnapshotCombinedIO = value.getLastCurrencySnapshotCombined
        getSnapshotInfo(lastSnapshotCombinedIO)
      case Right(value) =>
        val lastSnapshotCombinedIO = value.getLastCurrencySnapshotCombined
        getSnapshotInfo(lastSnapshotCombinedIO)
    }
  }
}

