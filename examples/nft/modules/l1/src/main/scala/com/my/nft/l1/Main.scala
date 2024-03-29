package com.my.nft.l1

import org.tessellation.BuildInfo
import org.tessellation.currency.l1.CurrencyL1App
import org.tessellation.schema.cluster.ClusterId

import java.util.UUID

object Main
  extends CurrencyL1App(
    "currency-l1",
    "currency L1 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    version = BuildInfo.version
  ) {}
