package com.my.reward_api.l1

import io.constellationnetwork.BuildInfo
import io.constellationnetwork.currency.l1.CurrencyL1App
import io.constellationnetwork.schema.cluster.ClusterId
import io.constellationnetwork.schema.semver.{MetagraphVersion, TessellationVersion}

import java.util.UUID

object Main
  extends CurrencyL1App(
    "custom-project-l1",
    "custom-project L1 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version),
    tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version)
  ) {}
