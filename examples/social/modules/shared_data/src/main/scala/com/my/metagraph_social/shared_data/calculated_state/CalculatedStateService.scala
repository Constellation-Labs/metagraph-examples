package com.my.metagraph_social.shared_data.calculated_state

import cats.effect.{Async, Ref}
import cats.syntax.all._
import com.my.metagraph_social.shared_data.types.States.SocialCalculatedState
import io.circe.Json
import io.circe.syntax.EncoderOps
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.hash.Hash

import java.nio.charset.StandardCharsets

trait CalculatedStateService[F[_]] {
  def get: F[CalculatedState]

  def set(
    snapshotOrdinal: SnapshotOrdinal,
    state          : SocialCalculatedState
  ): F[Boolean]

  def hash(
    state: SocialCalculatedState
  ): F[Hash]
}

object CalculatedStateService {
  def make[F[_] : Async](
    externalStorageService: ExternalStorageService[F]
  ): F[CalculatedStateService[F]] =
    Ref.of[F, CalculatedState](CalculatedState.empty).map { stateRef =>
      new CalculatedStateService[F] {
        override def get: F[CalculatedState] = stateRef.get

        override def set(
          snapshotOrdinal: SnapshotOrdinal,
          state          : SocialCalculatedState
        ): F[Boolean] = for {
          _ <- externalStorageService.set(snapshotOrdinal, state)
          response <- stateRef.update { currentState =>
            val currentCalculatedState = currentState.state
            val updatedUsers = state.users.foldLeft(currentCalculatedState.users) {
              case (acc, (address, value)) =>
                acc.updated(address, value)
            }
            CalculatedState(snapshotOrdinal, SocialCalculatedState(updatedUsers))
          }.as(true)
        } yield response

        override def hash(
          state: SocialCalculatedState
        ): F[Hash] = {
          def removeField(json: Json, fieldName: String): Json = {
            json.mapObject(_.filterKeys(_ != fieldName).mapValues(removeField(_, fieldName))).mapArray(_.map(removeField(_, fieldName)))
          }

          val jsonState = removeField(state.asJson, "postTime").deepDropNullValues.noSpaces
          Hash.fromBytes(jsonState.getBytes(StandardCharsets.UTF_8)).pure[F]
        }
      }
    }
}
