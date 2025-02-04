package my.weathermetagraph.shared.data.lifecycle

import cats.effect.Async
import fs2.io.net.Network
import my.weathermetagraph.shared.data.app.ApplicationConfig
import org.http4s.client.Client
import org.tessellation.json.JsonSerializer
import org.tessellation.node.shared.resources.MkHttpClient

trait Daemon[F[_]] {

  def startL0Daemon(
    calculatedStateService: CalculatedStateService[F]
  ): F[Unit]
}

object Daemon {

  def apply[F[_]: Async: Network: JsonSerializer](
    applicationConfig: ApplicationConfig
  ): Daemon[F] = new Daemon[F] {

    private def withHttpClient[A](
      useClient: Client[F] => F[A]
    ): F[A] = {
      val httpClient = MkHttpClient.forAsync[F].newEmber(applicationConfig.http4s.client)
      httpClient.use(useClient)
    }

    override def startL0Daemon(
      calculatedStateService: CalculatedStateService[F]
    ): F[Unit] = ???
  }
}
