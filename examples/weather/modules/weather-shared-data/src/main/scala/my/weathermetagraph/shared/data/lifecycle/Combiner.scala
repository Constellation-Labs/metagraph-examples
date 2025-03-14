package my.weathermetagraph.shared.data.lifecycle

import cats.effect.Async
import my.weathermetagraph.shared.data.domain.MetagraphState.{CalculatedState, OnChainState, WeatherDataUpdate}
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.security.signature.Signed

object Combiner {

  def apply[F[_]: Async](
    state:   DataState[OnChainState, CalculatedState],
    updates: List[Signed[WeatherDataUpdate]]
  ): F[DataState[OnChainState, CalculatedState]] = ???
}
