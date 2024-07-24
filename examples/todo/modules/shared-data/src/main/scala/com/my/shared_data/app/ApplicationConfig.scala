package com.my.shared_data.app

import scala.concurrent.duration._

import com.my.shared_data.app.ApplicationConfig.ML0Daemon

case class ApplicationConfig(
  http4s:    ApplicationConfig.Http4sConfig,
  ml0Daemon: ML0Daemon
)

object ApplicationConfig {

  case class Http4sConfig(client: Http4sConfig.Client)

  case class ML0Daemon(
    msg:      String,
    idleTime: FiniteDuration
  )

  object Http4sConfig {
    case class Client(timeout: FiniteDuration, idleTime: FiniteDuration)
  }
}
