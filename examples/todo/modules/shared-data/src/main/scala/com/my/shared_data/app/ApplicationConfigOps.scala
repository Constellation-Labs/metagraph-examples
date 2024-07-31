package com.my.shared_data.app

import cats.effect.kernel.Sync

import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

object ApplicationConfigOps {

  def readDefault[F[_]: Sync]: F[ApplicationConfig] =
    ConfigSource.default
      .loadF[F, ApplicationConfig]()
}
