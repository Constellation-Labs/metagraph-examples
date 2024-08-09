package com.my.validate_fee.l0

import cats.effect.{IO, Resource}
import cats.syntax.all._
import com.my.validate_fee.shared_data.ValidateFeeDataApplicationService
import com.my.validate_fee.shared_data.calculated_state.DataCalculatedStateService
import com.my.validate_fee.shared_data.types.Types.{CalculatedUpdateWithFeeState, UpdateWithFeeChainState, UpdateWithFee}
import org.tessellation.BuildInfo
import org.tessellation.currency.dataApplication._
import org.tessellation.currency.l0.CurrencyL0App
import org.tessellation.ext.cats.effect.ResourceIO
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.schema.semver.{MetagraphVersion, TessellationVersion}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.util.UUID

object Main extends CurrencyL0App(
  "currency-l0",
  "currency L0 node",
  ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
  metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version),
  tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version)
) {
  override def dataApplication: Option[Resource[IO, BaseDataApplicationL0Service[IO]]] =
    DataCalculatedStateService
      .make[IO, CalculatedUpdateWithFeeState]
      .map(svc => BaseDataApplicationL0Service(new L0Service(svc)))
      .asResource
      .some

  private class L0Service(override val calculatedStateService: DataCalculatedStateService[IO, CalculatedUpdateWithFeeState])
    extends ValidateFeeDataApplicationService[IO, L0NodeContext[IO]]
      with DataApplicationL0Service[IO, UpdateWithFee, UpdateWithFeeChainState, CalculatedUpdateWithFeeState] {

    override val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLoggerFromClass[IO](getClass)

    override def genesis: DataState[UpdateWithFeeChainState, CalculatedUpdateWithFeeState] =
      DataState(UpdateWithFeeChainState(List.empty), CalculatedUpdateWithFeeState(Map.empty))
  }
}
