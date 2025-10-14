package com.my.water_and_energy_usage.shared_data.validations

import cats.syntax.apply.catsSyntaxApplyOps
import cats.syntax.option.{catsSyntaxOptionId, none}
import com.my.water_and_energy_usage.shared_data.errors.Errors.EmptyUpdate
import com.my.water_and_energy_usage.shared_data.types.Types._
import com.my.water_and_energy_usage.shared_data.validations.TypeValidators._
import io.constellationnetwork.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import io.constellationnetwork.schema.address.Address
import io.constellationnetwork.security.signature.Signed


object Validations {
  private def getDeviceByAddress(
    address             : Address,
    maybeCalculatedState: Option[UsageUpdateCalculatedState]
  ): Option[DeviceCalculatedState] = {
    maybeCalculatedState
      .map(state => state.devices.get(address))
      .getOrElse(none)
  }

  def validateUsageUpdate(
    update              : UsageUpdate,
    maybeCalculatedState: Option[UsageUpdateCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] = {
    val energyUsage = update.energyUsage.getOrElse(EnergyUsage.empty)
    val waterUsage = update.waterUsage.getOrElse(WaterUsage.empty)

    if (energyUsage.timestamp <= 0 && waterUsage.timestamp <= 0) {
      EmptyUpdate.invalid
    } else {
      val address = update.address
      val maybeAggregatedUsage = getDeviceByAddress(address, maybeCalculatedState)

      if (energyUsage.timestamp > 0 && waterUsage.timestamp > 0) {
        validateEnergyUsageUpdate(energyUsage)
          .productR(validateWaterUsageUpdate(waterUsage))
          .productR(validateEnergyTimestamp(maybeAggregatedUsage, energyUsage))
          .productR(validateWaterTimestamp(maybeAggregatedUsage, waterUsage))
      } else if (energyUsage.timestamp > 0) {
        validateEnergyUsageUpdate(energyUsage)
          .productR(validateEnergyTimestamp(maybeAggregatedUsage, energyUsage))
      } else {
        validateWaterUsageUpdate(waterUsage)
          .productR(validateWaterTimestamp(maybeAggregatedUsage, waterUsage))
      }
    }
  }

  def validateUsageUpdateSigned(
    signedUpdate   : Signed[UsageUpdate],
    calculatedState: UsageUpdateCalculatedState,
    addresses      : List[Address]
  ): DataApplicationValidationErrorOr[Unit] =
    validateProvidedAddress(addresses, signedUpdate.value.address)
      .productR(validateUsageUpdate(signedUpdate.value, calculatedState.some))
}