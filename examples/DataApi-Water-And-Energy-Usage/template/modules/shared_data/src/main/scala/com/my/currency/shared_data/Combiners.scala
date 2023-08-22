package com.my.currency.shared_data

import com.my.currency.shared_data.Types.{AggregatedUsage, ENERGY_AND_WATER_UPDATE, ENERGY_USAGE_UPDATE, EnergyUsage, LastSnapshotRefs, LastTxnRefs, UpdateUsageTransaction, UsageState, UsageUpdate, WATER_USAGE_UPDATE, WaterUsage}
import com.my.currency.shared_data.Utils.getUsageUpdateHash
import monocle.Monocle.toAppliedFocusOps
import org.tessellation.schema.address.Address
import org.tessellation.security.signature.Signed

object Combiners {
  private def getUsageUpdateType(energyUsage: EnergyUsage, waterUsage: WaterUsage): String = {
    if (energyUsage.timestamp > 0 && waterUsage.timestamp > 0) {
      ENERGY_AND_WATER_UPDATE
    } else if (energyUsage.timestamp > 0) {
      ENERGY_USAGE_UPDATE
    } else {
      WATER_USAGE_UPDATE
    }
  }

  private def getUpdatedDeviceUsage(energyUsage: EnergyUsage, waterUsage: WaterUsage, acc: UsageState, address: Address): AggregatedUsage = {
    if (energyUsage.timestamp > 0 && waterUsage.timestamp > 0) {
      val currentState = acc.devices.getOrElse(address, AggregatedUsage.empty)
      currentState.
        focus(_.energy).modify { current =>
          EnergyUsage(current.usage + energyUsage.usage, energyUsage.timestamp)
        }
        .focus(_.water).modify { current =>
          WaterUsage(current.usage + waterUsage.usage, waterUsage.timestamp)
        }
    } else if (energyUsage.timestamp > 0) {
      val currentState = acc.devices.getOrElse(address, AggregatedUsage.empty)
      currentState.focus(_.energy).modify { current =>
        EnergyUsage(current.usage + energyUsage.usage, energyUsage.timestamp)
      }
    } else {
      val currentState = acc.devices.getOrElse(address, AggregatedUsage.empty)
      currentState.focus(_.water).modify { current =>
        WaterUsage(current.usage + waterUsage.usage, waterUsage.timestamp)
      }
    }
  }

  def combineUpdateUsage(signedUpdate: Signed[UsageUpdate], acc: UsageState, currentSnapshotOrdinal: Long): UsageState = {
    val update = signedUpdate.value
    val address = update.address
    val updateHash = getUsageUpdateHash(signedUpdate.value)

    val energyUsage = update.energyUsage.getOrElse(EnergyUsage.empty)
    val waterUsage = update.waterUsage.getOrElse(WaterUsage.empty)

    val updatedDeviceUsage = getUpdatedDeviceUsage(energyUsage, waterUsage, acc, address)
    val updateUsageType = getUsageUpdateType(energyUsage, waterUsage)

    val lastTxnRefs = acc.lastTxnRefs.getOrElse(address, LastTxnRefs.empty)
    val lastSnapshotRefs = acc.lastSnapshotRefs.getOrElse(address, null)

    val updateUsageTransaction = UpdateUsageTransaction(address, updateUsageType, energyUsage.usage, waterUsage.usage, currentSnapshotOrdinal)
    val newLastTxnRef = LastTxnRefs(currentSnapshotOrdinal, lastTxnRefs.txnOrdinal + 1, updateHash)

    if (lastSnapshotRefs == null) {
      acc.focus(_.devices).modify(_.updated(address, updatedDeviceUsage))
        .focus(_.transactions).modify(_.updated(updateHash, updateUsageTransaction))
        .focus(_.lastTxnRefs).modify(_.updated(address, newLastTxnRef))
        .focus(_.lastSnapshotRefs).modify(_.updated(address, LastSnapshotRefs.empty))
    } else if (lastTxnRefs.snapshotOrdinal == currentSnapshotOrdinal) {
      acc.focus(_.devices).modify(_.updated(address, updatedDeviceUsage))
        .focus(_.transactions).modify(_.updated(updateHash, updateUsageTransaction))
        .focus(_.lastTxnRefs).modify(_.updated(address, newLastTxnRef))
        .focus(_.lastSnapshotRefs).modify(_.updated(address, lastSnapshotRefs))
    } else {
      val createdLastRef = LastSnapshotRefs(lastTxnRefs.snapshotOrdinal, lastTxnRefs.hash)
      acc.focus(_.devices).modify(_.updated(address, updatedDeviceUsage))
        .focus(_.transactions).modify(_.updated(updateHash, updateUsageTransaction))
        .focus(_.lastTxnRefs).modify(_.updated(address, newLastTxnRef))
        .focus(_.lastSnapshotRefs).modify(_.updated(address, createdLastRef))
    }
  }
}