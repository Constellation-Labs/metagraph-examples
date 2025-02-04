package my.weathermetagraph.currency.l1

import my.weathermetagraph.buildinfo.BuildInfo
import org.tessellation.currency.l1.CurrencyL1App
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.schema.semver.{MetagraphVersion, TessellationVersion}

import java.util.UUID

object WeatherCurrencyL1App
    extends CurrencyL1App(
      name = "weather-currency-l1",
      header = "Weather Metagraph Currency L1 node",
      clusterId = ClusterId(UUID.fromString("30d19541-8068-4bdd-ab66-5613cf98e10f")),
      tessellationVersion = TessellationVersion.unsafeFrom(org.tessellation.BuildInfo.version),
      metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version)
    ) {}
