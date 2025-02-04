package my.weathermetagraph.shared.data.domain.repository

import cats.effect._
import doobie.hikari._
import my.weathermetagraph.shared.data.app.ApplicationConfig

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object DatabaseTransactor {

  def apply[F[_]: Async](
    config: ApplicationConfig.DatabaseConfig
  ): Resource[F, HikariTransactor[F]] =
    HikariTransactor.newHikariTransactor[F](
      config.driver,
      config.url,
      config.user,
      config.password,
      ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
    )
}
