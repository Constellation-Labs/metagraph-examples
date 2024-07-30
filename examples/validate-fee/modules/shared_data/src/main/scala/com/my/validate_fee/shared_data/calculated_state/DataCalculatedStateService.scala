package com.my.validate_fee.shared_data.calculated_state

import cats.Monoid
import cats.effect.Ref
import cats.effect.Async
import cats.syntax.all._
import io.circe.Encoder
import io.circe.syntax.EncoderOps
import org.tessellation.currency.dataApplication.DataCalculatedState
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.hash.Hash

import java.nio.charset.StandardCharsets

trait DataCalculatedStateService[F[_], A <: DataCalculatedState] {
  def getCalculatedState: F[(SnapshotOrdinal, A)]

  def setCalculatedState(snapshotOrdinal: SnapshotOrdinal, state: A): F[Boolean]

  def hashCalculatedState(state: A): F[Hash]
}

object DataCalculatedStateService {

  def make[F[_] : Async, A <: DataCalculatedState : Monoid: Encoder]: F[DataCalculatedStateService[F, A]] = {
    Ref.of[F, (SnapshotOrdinal, A)](SnapshotOrdinal.MinValue -> Monoid[A].empty).map { stateRef =>
      new DataCalculatedStateService[F, A] {
        override def getCalculatedState: F[(SnapshotOrdinal, A)] = stateRef.get

        override def setCalculatedState(snapshotOrdinal: SnapshotOrdinal, state: A): F[Boolean] =
          stateRef.set((snapshotOrdinal, state)).as(true)

        override def hashCalculatedState(state: A): F[Hash] = {
          val jsonState = state.asJson.deepDropNullValues.noSpaces
          Hash.fromBytes(jsonState.getBytes(StandardCharsets.UTF_8)).pure[F]
        }
      }
    }
  }
}
