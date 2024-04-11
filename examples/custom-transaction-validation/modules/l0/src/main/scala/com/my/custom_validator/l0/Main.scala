package com.my.custom_validator.l0

import java.util.UUID
import org.tessellation.BuildInfo
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.schema.semver.MetagraphVersion
import org.tessellation.schema.semver.TessellationVersion

object Main
  extends CurrencyL0App(
    "custom-transaction-validation-l0",
    "custom-transaction-validation L0 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version),
    metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version),
  ) {
}
