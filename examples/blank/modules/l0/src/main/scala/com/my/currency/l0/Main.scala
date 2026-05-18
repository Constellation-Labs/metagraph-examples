package com.my.currency.l0

import cats.effect.{IO, Resource}
import io.constellationnetwork.BuildInfo
import io.constellationnetwork.currency.dataApplication.{BaseDataApplicationL0Service}
import io.constellationnetwork.currency.l0.CurrencyL0App
import io.constellationnetwork.schema.cluster.ClusterId
import io.constellationnetwork.security.SecurityProvider
import io.constellationnetwork.schema.semver.{MetagraphVersion, TessellationVersion}

import java.util.UUID

  object Main
    extends CurrencyL0App(
      "my-metagraph-l0",
      "my-metagraph L0 node",
      ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
      tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version),
      metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version)
    ) {
  }
