package com.my.currency.shared_data.validations

import com.my.currency.shared_data.errors.Errors.{EnergyNotPositive, EnergyUpdateOutdated, InvalidAddress, WaterNotPositive, WaterUpdateOutdated}
import com.my.currency.shared_data.types.Types.{DeviceCalculatedState, EnergyUsage, WaterUsage}
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.schema.address.Address

object TypeValidators {
  def validateEnergyUsageUpdate(energy: EnergyUsage): DataApplicationValidationErrorOr[Unit] =
    EnergyNotPositive.unless(energy.usage > 0L && energy.timestamp > 0L)

  def validateWaterUsageUpdate(water: WaterUsage): DataApplicationValidationErrorOr[Unit] =
    WaterNotPositive.unless(water.usage > 0L && water.timestamp > 0L)

  def validateEnergyTimestamp(maybeAggregatedUsage: Option[DeviceCalculatedState], energy: EnergyUsage): DataApplicationValidationErrorOr[Unit] =
    EnergyUpdateOutdated.when(maybeAggregatedUsage.exists(oldUsage => energy.usage <= 0 || energy.timestamp <= oldUsage.usages.energy.timestamp))

  def validateWaterTimestamp(maybeAggregatedUsage: Option[DeviceCalculatedState], water: WaterUsage): DataApplicationValidationErrorOr[Unit] =
    WaterUpdateOutdated.when(maybeAggregatedUsage.exists(oldUsage => water.usage <= 0 || water.timestamp <= oldUsage.usages.water.timestamp))

  def validateProvidedAddress(proofAddresses: List[Address], address: Address): DataApplicationValidationErrorOr[Unit] =
    InvalidAddress.unless(proofAddresses.contains(address))
}
