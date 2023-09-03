package com.my.currency.shared_data

import cats.effect.IO
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxApply, catsSyntaxValidatedIdBinCompat0, toFoldableOps, toTraverseOps}
import com.my.currency.shared_data.Types.{EnergyUsage, UsageState, UsageUpdate, WaterUsage}
import com.my.currency.shared_data.Errors.EmptyUpdate
import com.my.currency.shared_data.TypeValidators.{validateEnergyTimestamp, validateEnergyUsageUpdate, validateProvidedAddress, validateWaterTimestamp, validateWaterUsageUpdate}
import org.tessellation.currency.dataApplication.DataApplicationValidationError
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.security.SecurityProvider
import org.tessellation.security.signature.Signed


object Validations {
  def validateUsageUpdate(update: UsageUpdate, state: UsageState): IO[DataApplicationValidationErrorOr[Unit]] = {
    val energyUsage = update.energyUsage.getOrElse(EnergyUsage.empty)
    val waterUsage = update.waterUsage.getOrElse(WaterUsage.empty)
    val address = update.address

    if (energyUsage.timestamp == 0 && waterUsage.timestamp == 0) {
      EmptyUpdate.asInstanceOf[DataApplicationValidationError].invalidNec.pure[IO]
    } else if (energyUsage.timestamp > 0 && waterUsage.timestamp > 0) {
      val validateTimestamp = validateEnergyTimestamp(state.devices.get(address), energyUsage).productR(validateWaterTimestamp(state.devices.get(address), waterUsage))
      IO {
        validateEnergyUsageUpdate(energyUsage).productR(validateWaterUsageUpdate(waterUsage)).productR(validateTimestamp)
      }
    } else if (energyUsage.timestamp > 0) {
      val validateTimestamp = validateEnergyTimestamp(state.devices.get(address), energyUsage)
      IO {
        validateEnergyUsageUpdate(energyUsage).productR(validateTimestamp)
      }
    } else if (waterUsage.timestamp > 0) {
      val validateTimestamp = validateWaterTimestamp(state.devices.get(address), waterUsage)
      IO {
        validateWaterUsageUpdate(waterUsage).productR(validateTimestamp)
      }
    } else {
      ().validNec.pure[IO]
    }
  }

  def validateUsageUpdateSigned(signedUpdate: Signed[UsageUpdate], oldState: UsageState)(implicit sp: SecurityProvider[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val addressesIO = signedUpdate.proofs
      .map(_.id)
      .toList
      .traverse(_.toAddress[IO])

    val validateAddress = addressesIO.map { addresses => validateProvidedAddress(addresses, signedUpdate.value.address) }
    val validateUpdate = validateUsageUpdate(signedUpdate.value, oldState)

    for {
      validatedAddress <- validateAddress
      validateUpdated <- validateUpdate
    } yield validatedAddress.productR(validateUpdated)
  }
}