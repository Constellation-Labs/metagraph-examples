package com.my.metagraph_l0

import java.util.UUID

import cats.effect.std.Supervisor
import cats.effect.{IO, Resource}
import cats.implicits._

import scala.concurrent.duration.DurationInt

import org.tessellation.currency.dataApplication._
import org.tessellation.currency.l0.CurrencyL0App
import org.tessellation.ext.cats.effect.ResourceIO
import org.tessellation.json.JsonSerializer
import org.tessellation.node.shared.domain.Daemon
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.schema.semver.{MetagraphVersion, TessellationVersion}
import org.tessellation.security.{Hasher, SecurityProvider}

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
      tessellationVersion = TessellationVersion.unsafeFrom(org.tessellation.BuildInfo.version),
      metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version)
    ) {

  override def dataApplication: Option[Resource[IO, BaseDataApplicationL0Service[IO]]] = (for {
    config                                           <- ApplicationConfigOps.readDefault[IO].asResource
    implicit0(logger: SelfAwareStructuredLogger[IO]) <- Slf4jLogger.create[IO].asResource
    implicit0(supervisor: Supervisor[IO])            <- Supervisor[IO]
    implicit0(json2bin: JsonSerializer[IO])          <- JsonBinaryCodec.forSync[IO].asResource
    implicit0(hasher: Hasher[IO])                    <- Hasher.forJson[IO].pure[IO].asResource
    implicit0(sp: SecurityProvider[IO])              <- SecurityProvider.forAsync[IO]
    l0Service <- ML0Service.make[IO].asResource
  } yield l0Service).some
}
