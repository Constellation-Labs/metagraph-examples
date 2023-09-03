package com.my.currency.shared_data

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.tessellation.currency.dataApplication.{DataState, DataUpdate}
import org.tessellation.schema.address.Address

object Types {
  val ENERGY_AND_WATER_UPDATE = "EnergyAndWaterUsage"
  val ENERGY_USAGE_UPDATE = "EnergyUsage"
  val WATER_USAGE_UPDATE = "WaterUsage"
  @derive(decoder, encoder)
  sealed trait Usage extends DataUpdate {
    val timestamp: Long
    val usage: Long
  }

  @derive(decoder, encoder)
  case class EnergyUsage(usage: Long, timestamp: Long) extends Usage

  object EnergyUsage {
    def empty: EnergyUsage = EnergyUsage(0L, 0L)
  }

  @derive(decoder, encoder)
  case class WaterUsage(usage: Long, timestamp: Long) extends Usage

  object WaterUsage {
    def empty: WaterUsage = WaterUsage(0L, 0L)
  }

  @derive(decoder, encoder)
  case class AggregatedUsage(energy: EnergyUsage, water: WaterUsage)

  object AggregatedUsage {
    def empty: AggregatedUsage = AggregatedUsage(EnergyUsage.empty, WaterUsage.empty)
  }

  @derive(decoder, encoder)
  case class UsageUpdate(address: Address, energyUsage: Option[EnergyUsage], waterUsage: Option[WaterUsage]) extends DataUpdate

  @derive(decoder, encoder)
  case class LastTxnRefs(snapshotOrdinal: Long, txnOrdinal: Long, hash: String)

  object LastTxnRefs {
    def empty: LastTxnRefs = LastTxnRefs(0, 0, "0000000000000000000000000000000000000000000000000000000000000000")
  }

  @derive(decoder, encoder)
  case class LastSnapshotRefs(ordinal: Long, hash: String)

  object LastSnapshotRefs {
    def empty: LastSnapshotRefs = LastSnapshotRefs(0, "0000000000000000000000000000000000000000000000000000000000000000")
  }

  @derive(decoder, encoder)
  case class UsageState(devices: Map[Address, AggregatedUsage], transactions: Map[String, UpdateUsageTransaction], lastTxnRefs: Map[Address, LastTxnRefs], lastSnapshotRefs: Map[Address, LastSnapshotRefs]) extends DataState
  @derive(decoder, encoder)
  case class UpdateUsageTransaction(owner: Address, transactionType: String, energyUpdateAmount: Long, waterUpdateAmount: Long, snapshotOrdinal: Long)
}
