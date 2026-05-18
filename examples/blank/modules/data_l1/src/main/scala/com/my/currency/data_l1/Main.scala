package com.my.currency.data_l1

import java.util.UUID
import io.constellationnetwork.BuildInfo
import io.constellationnetwork.currency.l1.CurrencyL1App
import io.constellationnetwork.schema.cluster.ClusterId
import io.constellationnetwork.schema.semver.{MetagraphVersion, TessellationVersion}

object Main
    extends CurrencyL1App(
      "my-metagraph-data_l1",
      "my-metagraph data L1 data node",
      ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
      tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version),
      metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version)
    ) {
    /*
    * To implement a data-l1 layer, you need to override the dataApplication function in CurrencyL1App:
    * 
    * override def dataApplication: Option[Resource[IO, BaseDataApplicationL1Service[IO]]]
    *
    * By default, this function returns None. If you do not provide an implementation, it will not function as a data layer,
    * and will instead operate as a normal currency layer.
    */

}
