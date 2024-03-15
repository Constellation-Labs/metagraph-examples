package com.my.reward_api.l1

import org.tessellation.BuildInfo
import org.tessellation.currency.l1.CurrencyL1App
import org.tessellation.schema.cluster.ClusterId

import java.util.UUID

object Main
  extends CurrencyL1App(
    "custom-project-l1",
    "custom-project L1 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    version = BuildInfo.version
  ) {}
