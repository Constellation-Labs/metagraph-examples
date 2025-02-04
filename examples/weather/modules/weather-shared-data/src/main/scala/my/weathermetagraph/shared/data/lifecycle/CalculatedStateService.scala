package my.weathermetagraph.shared.data.lifecycle

import cats.effect._
import cats.implicits._
import doobie.Transactor
import io.circe.Json
import io.circe.syntax.EncoderOps
import my.weathermetagraph.shared.data.domain.MetagraphState._
import my.weathermetagraph.shared.data.domain.repository.WeatherRepository
import org.tessellation.json.JsonSerializer
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.hash.Hash

final class CalculatedStateService[F[_]: Async](
  implicit stateRef: Ref[F, CalculatedState],
  xa:                Transactor[F],
  jsonSerializer:    JsonSerializer[F]) {

  private val repository = new WeatherRepository[F]

  def get: F[CalculatedState] = stateRef.get

  def update(
    snapshotOrdinal: SnapshotOrdinal,
    state:           CalculatedState
  ): F[Boolean] = {
    val records = state.states.collect {
      case WeatherCalculatedState(updates) => updates.map(_.toRecord(snapshotOrdinal))
    }

    stateRef.update { currentState =>
      CalculatedState(currentState.states ++ state.states)
    } *> repository.insertAll(records.flatten)
  }

  def hash(
    state: CalculatedState
  ): F[Hash] =
    JsonSerializer[F].serialize[Json](state.asJson).map(Hash.fromBytes)
}

object CalculatedStateService {

  def apply[F[_]: Async: JsonSerializer](
    implicit xa: Transactor[F]
  ): F[CalculatedStateService[F]] =
    Ref.of[F, CalculatedState](CalculatedState.genesis).map { implicit stateRef =>
      new CalculatedStateService[F]
    }
}
