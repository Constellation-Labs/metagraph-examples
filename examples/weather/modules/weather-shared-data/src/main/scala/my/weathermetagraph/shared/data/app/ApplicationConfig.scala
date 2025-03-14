package my.weathermetagraph.shared.data.app

import cats.effect.kernel.Sync
import org.tessellation.node.shared.config.types.HttpClientConfig
import pureconfig.generic.semiauto.deriveReader
import pureconfig.module.catseffect.syntax._
import pureconfig.{ConfigReader, ConfigSource}

import scala.concurrent.duration.FiniteDuration

/** Configuration for the application implicitly converted from the configuration file.
  *
  * @param database
  *   The configuration for the database.
  * @param weatherApi
  *   The configuration for the weather API.
  */
case class ApplicationConfig(
  database:   ApplicationConfig.DatabaseConfig,
  http4s:     ApplicationConfig.Http4sConfig,
  weatherApi: ApplicationConfig.WeatherApiConfig)

/** Companion object for the [[ApplicationConfig]] class. */
object ApplicationConfig {

  /** Configuration for the HTTP client implicitly converted from the configuration file.
    *
    * @param client
    *   The configuration for the HTTP client.
    */
  case class Http4sConfig(
    client: HttpClientConfig)

  /** Configuration for the database implicitly converted from the configuration file.
    *
    * @param driver
    *   The driver of the database. For example, "org.postgresql.Driver".
    * @param url
    *   The URL of the database. For example, "jdbc:postgresql://localhost:5432/weather".
    * @param user
    *   The user to access the database.
    * @param password
    *   The password to access the database.
    */
  case class DatabaseConfig(
    driver:   String,
    url:      String,
    user:     String,
    password: String)

  /** Configuration for the weather API implicitly converted from the configuration file.
    *
    * @param url
    *   The URL of the weather API.
    * @param apiKey
    *   The API key to access the weather API.
    * @param daemonInterval
    *   The interval in which the daemon will fetch the weather data.
    */
  case class WeatherApiConfig(
    url:            String,
    apiKey:         String,
    daemonInterval: FiniteDuration)

  /** Read the configuration from the default configuration file (usually main/scala/resources/application.conf).
    */
  def readDefault[F[_]: Sync]: F[ApplicationConfig] =
    ConfigSource.default
      .loadF[F, ApplicationConfig]()

  /** Implicit readers to convert the parameters from the configuration file to their corresponding case class */
  implicit val applicationConfigReader: ConfigReader[ApplicationConfig] = deriveReader
  implicit val http4sConfigReader: ConfigReader[ApplicationConfig.Http4sConfig] = deriveReader
  implicit val http4sClientConfigReader: ConfigReader[HttpClientConfig] = deriveReader
  implicit val databaseConfigReader: ConfigReader[ApplicationConfig.DatabaseConfig] = deriveReader
  implicit val weatherApiConfigReader: ConfigReader[ApplicationConfig.WeatherApiConfig] = deriveReader

}
