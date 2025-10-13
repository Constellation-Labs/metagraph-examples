package com.my.metagraph_l0

import java.util.UUID

import cats.effect.std.Supervisor
import cats.effect.{IO, Resource}
import cats.implicits._


import io.constellationnetwork.currency.dataApplication._
import io.constellationnetwork.currency.l0.CurrencyL0App
import io.constellationnetwork.ext.cats.effect.ResourceIO
import io.constellationnetwork.json.JsonSerializer
import io.constellationnetwork.node.shared.domain.Daemon
import io.constellationnetwork.schema.cluster.ClusterId
import io.constellationnetwork.schema.semver.{MetagraphVersion, TessellationVersion}
import io.constellationnetwork.security.{Hasher, SecurityProvider}

import com.my.buildinfo.BuildInfo
import com.my.shared_data.app.{ApplicationConfig, ApplicationConfigOps}
import com.my.shared_data.lib.JsonBinaryCodec

import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}

object Main
    extends CurrencyL0App(
      name = "metagraph-l0",
      header = "Metagraph L0 node",
      clusterId = ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
      tessellationVersion = TessellationVersion.unsafeFrom(io.constellationnetwork.BuildInfo.version),
      metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version)
    ) {

  override def dataApplication: Option[Resource[IO, BaseDataApplicationL0Service[IO]]] = (for {
    config                                           <- ApplicationConfigOps.readDefault[IO].asResource
    implicit0(logger: SelfAwareStructuredLogger[IO]) <- Slf4jLogger.create[IO].asResource
    implicit0(supervisor: Supervisor[IO])            <- Supervisor[IO]
    implicit0(json2bin: JsonSerializer[IO])          <- JsonBinaryCodec.forSync[IO].asResource
    implicit0(hasher: Hasher[IO])                    <- Hasher.forJson[IO].pure[IO].asResource
    implicit0(sp: SecurityProvider[IO])              <- SecurityProvider.forAsync[IO]
    _                                                <- makePeriodicConfigDaemon(config).asResource
    l0Service <- ML0Service.make[IO].asResource
  } yield l0Service).some

  private def makePeriodicConfigDaemon(
    config: ApplicationConfig
  )(implicit sup: Supervisor[IO], logger: Logger[IO]): IO[Unit] =
    Daemon
      .periodic[IO](
        logger.info(s"[Daemon] ${config.ml0Daemon.msg}"),
        config.ml0Daemon.idleTime
      )
      .start
}
