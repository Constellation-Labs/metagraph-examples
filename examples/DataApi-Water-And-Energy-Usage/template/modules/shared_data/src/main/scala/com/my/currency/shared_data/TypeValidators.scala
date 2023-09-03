package com.my.currency.shared_data

import cats.implicits.catsSyntaxValidatedIdBinCompat0
import com.my.currency.shared_data.Types.{AggregatedUsage, EnergyUsage, WaterUsage}
import com.my.currency.shared_data.Errors.{EnergyNotPositive, EnergyUpdateOutdated, InvalidAddress, WaterNotPositive, WaterUpdateOutdated}
import org.tessellation.currency.dataApplication.DataApplicationValidationError
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.schema.address.Address

object TypeValidators {
  def validateEnergyUsageUpdate(energy: EnergyUsage): DataApplicationValidationErrorOr[Unit] =
    if (energy.usage > 0L && energy.timestamp > 0L)
      ().validNec
    else
      EnergyNotPositive.asInstanceOf[DataApplicationValidationError].invalidNec

  def validateWaterUsageUpdate(water: WaterUsage): DataApplicationValidationErrorOr[Unit] =
    if (water.usage > 0L && water.timestamp > 0L)
      ().validNec
    else
      WaterNotPositive.asInstanceOf[DataApplicationValidationError].invalidNec

  /**
   * This validation prevents old updates from being accepted after a newer update has already been processed.
   * There is also a case where old blocks can be sent to L0 consensus by different nodes with duplicate values
   * which can result in "double spends". This validation provides enough uniqueness to prevent that scenario.
   * In other implementations, a unique field of some sort should be provided to validate whether an update has been
   * already included in state or not.
   */
  def validateEnergyTimestamp(maybeState: Option[AggregatedUsage], energy: EnergyUsage): DataApplicationValidationErrorOr[Unit] = {
    maybeState.map { total =>
      if (energy.timestamp > total.energy.timestamp && energy.usage > 0L)
        ().validNec
      else
        EnergyUpdateOutdated.asInstanceOf[DataApplicationValidationError].invalidNec
    }.getOrElse(().validNec)
  }

  /**
   * This validation provides the same assurance as the `validateEnergyTimestamp` for WaterUsage. See comment above for details.
   */
  def validateWaterTimestamp(maybeState: Option[AggregatedUsage], water: WaterUsage): DataApplicationValidationErrorOr[Unit] =
    maybeState.map { total =>
      if (water.timestamp > total.energy.timestamp && water.usage > 0L)
        ().validNec
      else
        WaterUpdateOutdated.asInstanceOf[DataApplicationValidationError].invalidNec
    }.getOrElse(().validNec)

  def validateProvidedAddress(proofAddresses: List[Address], address: Address): DataApplicationValidationErrorOr[Unit] = {
    if (proofAddresses.contains(address)) {
      ().validNec
    } else {
      InvalidAddress.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }
}
