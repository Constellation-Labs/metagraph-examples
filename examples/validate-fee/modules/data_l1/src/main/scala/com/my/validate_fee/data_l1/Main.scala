package com.my.validate_fee.data_l1

import cats.Applicative
import cats.effect.{IO, Resource}
import cats.syntax.all._
import com.my.validate_fee.shared_data.FeeCalculators._
import com.my.validate_fee.shared_data.ValidateFeeDataApplicationService
import com.my.validate_fee.shared_data.calculated_state.DataCalculatedStateService
import com.my.validate_fee.shared_data.types.Types._
import io.circe.syntax.EncoderOps
import org.tessellation.BuildInfo
import org.tessellation.currency.dataApplication._
import org.tessellation.currency.l1.CurrencyL1App
import org.tessellation.currency.schema.EstimatedFee
import org.tessellation.ext.cats.effect.ResourceIO
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.schema.semver.{MetagraphVersion, TessellationVersion}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.util.UUID

object Main
  extends CurrencyL1App(
    "currency-data_l1",
    "currency data L1 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version),
    tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version)
  ) {

  override def dataApplication: Option[Resource[IO, BaseDataApplicationL1Service[IO]]] =
    DataCalculatedStateService
      .make[IO, CalculatedUpdateWithFeeState]
      .map(ss => BaseDataApplicationL1Service(new L1Service(ss)))
      .asResource
      .some

  private class L1Service(override val calculatedStateService: DataCalculatedStateService[IO, CalculatedUpdateWithFeeState])
    extends ValidateFeeDataApplicationService[IO, L1NodeContext[IO]]
      with DataApplicationL1Service[IO, UpdateWithFee, UpdateWithFeeChainState, CalculatedUpdateWithFeeState] {

    override val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLoggerFromClass[IO](getClass)

    override def estimateFee(gsOrdinal: SnapshotOrdinal)(update: UpdateWithFee)(implicit context: L1NodeContext[IO], A: Applicative[IO]): IO[EstimatedFee] =
      update
        .pure[IO]
        .map(calculateFee)
        .map(EstimatedFee(_, update.fee.destination))
        .flatTap(fee => logger.debug(s"estimateFee: $gsOrdinal\n${update.asJson.spaces2}\n${fee.show}"))
  }
}

