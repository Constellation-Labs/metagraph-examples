package com.my.nft.l0

import java.util.UUID

import cats.effect.{IO, Resource}
import cats.syntax.option._

import org.tessellation.BuildInfo
import org.tessellation.currency.dataApplication._
import org.tessellation.currency.l0.CurrencyL0App
import org.tessellation.ext.cats.effect.ResourceIO
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.schema.semver.{MetagraphVersion, TessellationVersion}
import org.tessellation.security.SecurityProvider

import com.my.nft.shared_data.schema.NFTUpdatesCalculatedState
import com.my.nft.shared_data.{CombinerService, ValidationService}

import io.constellationnetwork.metagraph_sdk.lifecycle.CheckpointService

object Main
    extends CurrencyL0App(
      "currency-l0",
      "currency L0 node",
      ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
      metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version),
      tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version)
    ) {

  override def dataApplication: Option[Resource[IO, BaseDataApplicationL0Service[IO]]] = (for {
    implicit0(sp: SecurityProvider[IO]) <- SecurityProvider.forAsync[IO]
    checkpointService <- CheckpointService
      .make[IO, NFTUpdatesCalculatedState](NFTUpdatesCalculatedState.genesis)
      .asResource
    validationService = ValidationService.make[IO]
    combinerService = CombinerService.make[IO]
    l0Service = ML0Service.make[IO](checkpointService, combinerService, validationService)
  } yield l0Service).some
}
