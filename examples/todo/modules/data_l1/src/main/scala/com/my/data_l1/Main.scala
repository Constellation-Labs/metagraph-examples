package com.my.data_l1

import java.util.UUID

import cats.effect.{IO, Resource}
import cats.syntax.all._

import com.my.buildinfo.BuildInfo
import com.my.shared_data.app.ApplicationConfigOps

import io.constellationnetwork.currency.dataApplication._
import io.constellationnetwork.currency.l1.CurrencyL1App
import io.constellationnetwork.ext.cats.effect.ResourceIO
import io.constellationnetwork.schema.cluster.ClusterId
import io.constellationnetwork.schema.semver.{MetagraphVersion, TessellationVersion}

object Main
    extends CurrencyL1App(
      name = "data-app-l1",
      header = "Metagraph Data L1 node",
      clusterId = ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
      tessellationVersion = TessellationVersion.unsafeFrom(io.constellationnetwork.BuildInfo.version),
      metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version)
    ) {

  override def dataApplication: Option[Resource[IO, BaseDataApplicationL1Service[IO]]] = (for {
    config    <- ApplicationConfigOps.readDefault[IO].asResource
    l1Service <- DataL1Service.make[IO].asResource
  } yield l1Service).some
}
