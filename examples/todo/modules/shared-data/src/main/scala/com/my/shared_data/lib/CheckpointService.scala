package com.my.shared_data.lib

import cats.effect.{Concurrent, Ref}
import cats.syntax.all._

import org.typelevel.log4cats.Logger

trait CheckpointService[F[_], S] {
  def get: F[Checkpoint[S]]
  def set(checkpoint: Checkpoint[S]): F[Boolean]
}

object CheckpointService {

  def make[F[_]: Concurrent: Logger, S](state: S): F[CheckpointService[F, S]] =
    Ref
      .of[F, Checkpoint[S]](Checkpoint.genesis(state))
      .map { state =>
        new CheckpointService[F, S] {
          override def get: F[Checkpoint[S]] =
            state.get

          override def set(checkpoint: Checkpoint[S]): F[Boolean] =
            state
              .set(checkpoint)
              .as(true)
              .handleErrorWith { err =>
                Logger[F].warn(s"Checkpoint set failed with error: $err") *> false.pure[F]
              }
        }
      }
}
