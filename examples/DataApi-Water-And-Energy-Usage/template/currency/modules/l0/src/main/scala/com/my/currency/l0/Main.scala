package com.my.currency.l0

import cats.effect.IO
import org.tessellation.BuildInfo
import org.tessellation.currency.BaseDataApplicationL0Service
import org.tessellation.currency.l0.CurrencyL0App
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.security.SecurityProvider

import java.util.UUID

  object Main
    extends CurrencyL0App(
      "currency-l0",
      "currency L0 node",
      ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
      version = BuildInfo.version
    ) {
    def dataApplication: Option[BaseDataApplicationL0Service[IO]] = None

    def rewards(implicit sp: SecurityProvider[IO]) = None
  }
