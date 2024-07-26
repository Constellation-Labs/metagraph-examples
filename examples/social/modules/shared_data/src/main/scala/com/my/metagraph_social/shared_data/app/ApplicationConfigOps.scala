package com.my.metagraph_social.shared_data.app

import cats.effect.kernel.Sync
import pureconfig._
import pureconfig.generic.semiauto.deriveReader
import pureconfig.module.catseffect.syntax._

object ApplicationConfigOps {

  import ConfigReaders._

  def readDefault[F[_] : Sync]: F[ApplicationConfig] =
    ConfigSource.default
      .loadF[F, ApplicationConfig]()
}

object ConfigReaders {
  implicit val postgresDatabaseConfigReader: ConfigReader[ApplicationConfig.PostgresDatabase] = deriveReader
  implicit val applicationConfigReader: ConfigReader[ApplicationConfig] = deriveReader
}