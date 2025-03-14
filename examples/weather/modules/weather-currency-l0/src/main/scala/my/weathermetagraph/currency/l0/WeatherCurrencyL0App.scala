package my.weathermetagraph.currency.l0

import cats.data.NonEmptyList
import cats.effect.{Async, IO, Resource}
import cats.syntax.all._
import doobie.util.transactor.Transactor
import io.constellationnetwork.metagraph_sdk.MetagraphCommonService
import io.constellationnetwork.metagraph_sdk.lifecycle.CheckpointService
import io.constellationnetwork.metagraph_sdk.std.Checkpoint
import my.weathermetagraph.buildinfo.BuildInfo
import my.weathermetagraph.shared.data.app.ApplicationConfig
import my.weathermetagraph.shared.data.domain.MetagraphState._
import my.weathermetagraph.shared.data.domain.repository.DatabaseTransactor
import my.weathermetagraph.shared.data.lifecycle.{CalculatedStateService, Combiner}
import org.http4s.HttpRoutes
import org.tessellation.currency.dataApplication._
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.l0.CurrencyL0App
import org.tessellation.ext.cats.effect.ResourceIO
import org.tessellation.json.JsonSerializer
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.schema.semver.{MetagraphVersion, TessellationVersion}
import org.tessellation.security.hash.Hash
import org.tessellation.security.signature.Signed

import java.util.UUID

object WeatherCurrencyL0App
    extends CurrencyL0App(
      name = "weather-currency-l0",
      header = "Weather Metagraph Currency L0 node",
      clusterId = ClusterId(UUID.fromString("55fa2f4b-a80c-40d4-b3fe-25befba9c8a5")),
      tessellationVersion = TessellationVersion.unsafeFrom(org.tessellation.BuildInfo.version),
      metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version)
    ) {

  override def dataApplication: Option[Resource[IO, BaseDataApplicationL0Service[IO]]] =
    Some {
      for {
        config            <- ApplicationConfig.readDefault[IO].map(_.database).asResource
        jsonSerializer    <- JsonSerializer.forSync[IO].asResource
        transactor        <- DatabaseTransactor[IO](config)
        checkpointService <- CheckpointService.make[IO, CalculatedState](CalculatedState.genesis).asResource
        calculatedStateService <- {
          implicit val js: JsonSerializer[IO] = jsonSerializer
          implicit val xa: Transactor[IO] = transactor
          CalculatedStateService[IO].asResource
        }
        l0Service = WeatherCurrencyL0Service[IO](calculatedStateService, checkpointService)
      } yield l0Service
    }

  object WeatherCurrencyL0Service {

    def apply[F[+_]: Async](
      calculatedStateService: CalculatedStateService[F],
      checkpointService:      CheckpointService[F, CalculatedState]
    ): BaseDataApplicationL0Service[F] =
      BaseDataApplicationL0Service[F, WeatherDataUpdate, OnChainState, CalculatedState](
        new MetagraphCommonService[F, WeatherDataUpdate, OnChainState, CalculatedState]
          with DataApplicationL0Service[F, WeatherDataUpdate, OnChainState, CalculatedState] {
          override def genesis: DataState[OnChainState, CalculatedState] = DataState(OnChainState.genesis, CalculatedState.genesis)

          override def validateData(
            state:            DataState[OnChainState, CalculatedState],
            updates:          NonEmptyList[Signed[WeatherDataUpdate]]
          )(implicit context: L0NodeContext[F]
          ): F[DataApplicationValidationErrorOr[Unit]] = ().validNec.pure[F]

          override def validateUpdate(
            update:           WeatherDataUpdate
          )(implicit context: L0NodeContext[F]
          ): F[DataApplicationValidationErrorOr[Unit]] = ().validNec.pure[F]

          override def combine(
            state:            DataState[OnChainState, CalculatedState],
            updates:          List[Signed[WeatherDataUpdate]]
          )(implicit context: L0NodeContext[F]
          ): F[DataState[OnChainState, CalculatedState]] = Combiner[F](state, updates)

          override def routes(
            implicit context: L0NodeContext[F]
          ): HttpRoutes[F] = HttpRoutes.empty

          override def getCalculatedState(
            implicit context: L0NodeContext[F]
          ): F[(SnapshotOrdinal, CalculatedState)] = checkpointService.get.map {
            case Checkpoint(ordinal, state) => ordinal -> state
          }

          override def setCalculatedState(
            ordinal:          SnapshotOrdinal,
            state:            CalculatedState
          )(implicit context: L0NodeContext[F]
          ): F[Boolean] = calculatedStateService.update(ordinal, state)

          override def hashCalculatedState(
            state:            CalculatedState
          )(implicit context: L0NodeContext[F]
          ): F[Hash] = calculatedStateService.hash(state)
        }
      )
  }

}
