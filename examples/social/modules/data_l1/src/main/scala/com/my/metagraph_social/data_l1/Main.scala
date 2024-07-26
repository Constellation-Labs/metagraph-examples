package com.my.metagraph_social.data_l1

import cats.effect.{IO, Resource}
import cats.syntax.all._
import com.my.metagraph_social.shared_data.app.ApplicationConfigOps
import com.my.metagraph_social.shared_data.calculated_state.CalculatedStateService
import com.my.metagraph_social.shared_data.calculated_state.postgres.PostgresService
import com.my.metagraph_social.shared_data.types.codecs.JsonBinaryCodec
import org.tessellation.BuildInfo
import org.tessellation.currency.dataApplication._
import org.tessellation.currency.l1.CurrencyL1App
import org.tessellation.ext.cats.effect.ResourceIO
import org.tessellation.json.JsonSerializer
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.schema.semver.{MetagraphVersion, TessellationVersion}

import java.util.UUID

object Main
  extends CurrencyL1App(
    "currency-data_l1",
    "currency data L1 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version),
    tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version)
  ) {
  override def dataApplication: Option[Resource[IO, BaseDataApplicationL1Service[IO]]] = (for {
    implicit0(json2bin: JsonSerializer[IO]) <- JsonBinaryCodec.forSync[IO].asResource
    config <- ApplicationConfigOps.readDefault[IO].asResource
    dbCredentials = config.postgresDatabase
    postgresService <- PostgresService.make[IO](dbCredentials.url, dbCredentials.user, dbCredentials.password)
    calculatedStateService <- CalculatedStateService.make[IO](postgresService).asResource
    l1Service <- DataL1Service.make[IO](calculatedStateService).asResource
  } yield l1Service).some
}
