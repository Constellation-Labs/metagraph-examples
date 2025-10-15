package com.my.metagraph_social.data_l1

import cats.effect.{IO, Resource}
import cats.syntax.all._
import com.my.metagraph_social.shared_data.types.codecs.JsonBinaryCodec
import io.constellationnetwork.BuildInfo
import io.constellationnetwork.currency.dataApplication._
import io.constellationnetwork.currency.l1.CurrencyL1App
import io.constellationnetwork.ext.cats.effect.ResourceIO
import io.constellationnetwork.json.JsonSerializer
import io.constellationnetwork.schema.cluster.ClusterId
import io.constellationnetwork.schema.semver.{MetagraphVersion, TessellationVersion}

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
    l1Service <- DataL1Service.make[IO].asResource
  } yield l1Service).some
}
