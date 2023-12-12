package com.my.currency.shared_data.combiners

import com.my.currency.shared_data.Utils.getUsageUpdateHash
import com.my.currency.shared_data.types.Types._
import eu.timepit.refined.types.numeric.NonNegLong
import monocle.Monocle.toAppliedFocusOps
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.address.Address
import org.tessellation.security.signature.Signed

object Combiners {
  private def getUsageUpdateType(
    energyUsage: EnergyUsage,
    waterUsage : WaterUsage
  ): String = {
    if (energyUsage.timestamp > 0 && waterUsage.timestamp > 0) {
      EnergyAndWaterUpdate
    } else if (energyUsage.timestamp > 0) {
      EnergyUsageUpdate
    } else {
      WaterUsageUpdate
    }
  }

  private def getUpdatedDeviceUsage(
    energyUsage: EnergyUsage,
    waterUsage : WaterUsage,
    acc        : DataState[UsageUpdateState, UsageUpdateCalculatedState],
    address    : Address
  ): AggregatedUsage = {
    if (energyUsage.timestamp > 0 && waterUsage.timestamp > 0) {
      val deviceCalculatedState = acc.calculated.devices.getOrElse(address, DeviceCalculatedState.empty)
      deviceCalculatedState.usages
        .focus(_.energy).modify { current =>
          EnergyUsage(current.usage + energyUsage.usage, energyUsage.timestamp)
        }
        .focus(_.water).modify { current =>
          WaterUsage(current.usage + waterUsage.usage, waterUsage.timestamp)
        }
    } else if (energyUsage.timestamp > 0) {
      val deviceCalculatedState = acc.calculated.devices.getOrElse(address, DeviceCalculatedState.empty)
      deviceCalculatedState.usages
        .focus(_.energy).modify { current =>
          EnergyUsage(current.usage + energyUsage.usage, energyUsage.timestamp)
        }
    } else {
      val deviceCalculatedState = acc.calculated.devices.getOrElse(address, DeviceCalculatedState.empty)
      deviceCalculatedState.usages
        .focus(_.water).modify { current =>
          WaterUsage(current.usage + waterUsage.usage, waterUsage.timestamp)
        }
    }
  }

  def combineUpdateUsage(
    signedUpdate       : Signed[UsageUpdate],
    acc                : DataState[UsageUpdateState, UsageUpdateCalculatedState],
    lastSnapshotOrdinal: SnapshotOrdinal
  ): DataState[UsageUpdateState, UsageUpdateCalculatedState] = {
    val update = signedUpdate.value
    val address = update.address
    val updateHash = getUsageUpdateHash(signedUpdate.value)

    val energyUsage = update.energyUsage.getOrElse(EnergyUsage.empty)
    val waterUsage = update.waterUsage.getOrElse(WaterUsage.empty)

    val updatedDeviceUsage = getUpdatedDeviceUsage(energyUsage, waterUsage, acc, address)
    val updateUsageType = getUsageUpdateType(energyUsage, waterUsage)

    val currentSnapshotOrdinal: SnapshotOrdinal = SnapshotOrdinal(NonNegLong.unsafeFrom(lastSnapshotOrdinal.value.value + 1))

    val lastTxnRef = acc.calculated.devices.get(address).fold(TxnRef.empty)(_.currentTxnRef)
    val updateUsageTransaction = UpdateUsageTransaction(address, updateUsageType, energyUsage.usage, waterUsage.usage, lastTxnRef.txnSnapshotOrdinal, lastTxnRef.txnHash)
    val device = DeviceCalculatedState(updatedDeviceUsage, TxnRef(currentSnapshotOrdinal, updateHash))
    val devices = acc.calculated.devices.updated(address, device)

    val updates: List[UpdateUsageTransaction] = updateUsageTransaction :: acc.onChain.updates

    DataState(
      UsageUpdateState(updates),
      UsageUpdateCalculatedState(devices)
    )
  }
}