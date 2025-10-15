package com.my.custom_validator.l0

import java.util.UUID

import io.constellationnetwork.BuildInfo
import io.constellationnetwork.currency.l0.CurrencyL0App
import io.constellationnetwork.schema.cluster.ClusterId
import io.constellationnetwork.schema.semver.{MetagraphVersion, TessellationVersion}

object Main
  extends CurrencyL0App(
    "custom-transaction-validation-l0",
    "custom-transaction-validation L0 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version),
    metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version)
  ) {
}
