package my.weathermetagraph.shared.data.client

import cats.effect._
import fs2.io.net.Network
import io.circe.generic.auto._
import my.weathermetagraph.shared.data.client.WeatherClient._
import org.http4s.circe._
import org.http4s.client._
import org.http4s.{Method, Request, Uri}

/** Weather client to fetch data from the weather API (weatherapi.com)
  *
  * @param apiKey
  *   API key to access the weather API
  * @param baseUrl
  *   Base URL of the weather API
  * @param client
  *   HTTP client to make requests
  * @tparam F
  *   Effect type
  */
class WeatherClient[F[_]: Async: Network](
  apiKey:  String,
  baseUrl: String
)(implicit client: Client[F]) {

  /** Fetch the current weather for a given region
    *
    * @param region
    *   Region to fetch the weather for
    * @return
    *   Weather response
    */
  def fetchCurrentWeather(
    region: String
  ): F[WeatherResponse] = {
    val request = Request[F](
      Method.GET,
      Uri
        .unsafeFromString(baseUrl + "/current.json")
        .withQueryParam("key", apiKey)
        .withQueryParam("q", region)
    )

    client.expect[WeatherResponse](request)(jsonOf[F, WeatherResponse])
  }
}

/** Weather client companion object */
object WeatherClient {

  /** Weather condition
    *
    * @param text
    *   Weather condition text. For example: "Partly cloudy", "Sunny", "Rainy"
    */
  case class WeatherCondition(
    text: String)

  /** Current weather data
    *
    * @param temp_f
    *   Temperature in Fahrenheit
    * @param temp_c
    *   Temperature in Celsius
    * @param condition
    *   Weather condition. For example: "Partly cloudy", "Sunny", "Rainy"
    */
  case class CurrentWeather(
    temp_f:    Float,
    temp_c:    Float,
    condition: WeatherCondition)

  /** Weather response from the weather API
    *
    * @param location
    *   The location where the weather data was collected
    * @param current
    *   The current weather data
    */
  case class WeatherResponse(
    location: Location,
    current:  CurrentWeather)

  /** Represents a location where weather data is collected
    *
    * @param name
    *   The name of the location
    * @param region
    *   The region where the location is located
    * @param country
    *   The country where the location is located
    */
  case class Location(
    name:    String,
    region:  String,
    country: String)

}
