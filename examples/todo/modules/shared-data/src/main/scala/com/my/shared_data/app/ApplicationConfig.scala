package com.my.shared_data.app

import scala.concurrent.duration._

case class ApplicationConfig(
  http4s:             ApplicationConfig.Http4sConfig,
  syncDaemon:         ApplicationConfig.SyncDaemon,
  transactionMonitor: ApplicationConfig.TransactionMonitor
)

object ApplicationConfig {

  case class Http4sConfig(client: Http4sConfig.Client)

  case class SyncDaemon(
    idleTime: FiniteDuration
  )

  case class TransactionMonitor(
    idleTime: FiniteDuration
  )

  object Http4sConfig {
    case class Client(timeout: FiniteDuration, idleTime: FiniteDuration)
  }
}
