package com.my.metagraph_social.shared_data.calculated_state.postgres

import cats.effect._
import cats.syntax.all._
import com.my.metagraph_social.shared_data.calculated_state.{CalculatedState, ExternalStorageService}
import com.my.metagraph_social.shared_data.types.States.SocialCalculatedState
import doobie.hikari.HikariTransactor
import doobie.implicits._
import io.circe.parser._
import io.circe.syntax._
import org.tessellation.schema.SnapshotOrdinal

import scala.concurrent.ExecutionContext


object PostgresService {
  def make[F[_] : Async](url: String, user: String, password: String): Resource[F, ExternalStorageService[F]] = {

    val transactor = for {
      xa <- HikariTransactor.newHikariTransactor[F](
        "org.postgresql.Driver", url, user, password, ExecutionContext.global
      )
    } yield xa

    transactor.map { xa =>
      new ExternalStorageService[F] {
        override def get(ordinal: SnapshotOrdinal): F[CalculatedState] = {
          sql"SELECT state FROM calculated_states WHERE ordinal = ${ordinal.value.value}"
            .query[String]
            .map(decode[CalculatedState](_).getOrElse(throw new Exception("Decoding error")))
            .unique
            .transact(xa)
        }

        override def set(snapshotOrdinal: SnapshotOrdinal, state: SocialCalculatedState): F[Boolean] = {
          val calculatedState = CalculatedState(snapshotOrdinal, state)
          val jsonState = calculatedState.asJson.noSpaces

          sql"INSERT INTO calculated_states (ordinal, state) VALUES (${snapshotOrdinal.value.value}, $jsonState::jsonb) ON CONFLICT (ordinal) DO UPDATE SET state = EXCLUDED.state"
            .update
            .run
            .transact(xa)
            .map(_ > 0)
        }

        override def getLatest: F[CalculatedState] = {
          sql"SELECT state FROM calculated_states ORDER BY ordinal DESC LIMIT 1"
            .query[String]
            .map(decode[CalculatedState](_).getOrElse(throw new Exception("Decoding error")))
            .unique
            .transact(xa)
        }
      }
    }
  }
}
