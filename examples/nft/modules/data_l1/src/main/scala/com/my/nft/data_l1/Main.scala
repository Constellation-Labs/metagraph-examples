package com.my.nft.data_l1

import java.util.UUID

import cats.effect.{IO, Resource}
import cats.syntax.option._

import org.tessellation.BuildInfo
import org.tessellation.currency.dataApplication._
import org.tessellation.currency.l1.CurrencyL1App
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.schema.semver.{MetagraphVersion, TessellationVersion}
import org.tessellation.security.SecurityProvider

import com.my.nft.shared_data.ValidationService

object Main
    extends CurrencyL1App(
      "currency-data_l1",
      "currency data L1 node",
      ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
      metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version),
      tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version)
    ) {

  override def dataApplication: Option[Resource[IO, BaseDataApplicationL1Service[IO]]] = (for {
    implicit0(sp: SecurityProvider[IO]) <- SecurityProvider.forAsync[IO]
    validationService = ValidationService.make[IO]
    l1Service = DataL1Service.make[IO](validationService)
  } yield l1Service).some
}
