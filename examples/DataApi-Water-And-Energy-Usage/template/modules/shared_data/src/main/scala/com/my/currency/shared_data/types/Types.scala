package com.my.currency.shared_data.types

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.tessellation.currency.dataApplication.{DataCalculatedState, DataOnChainState, DataUpdate}
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.address.Address

object Types {
  val EnergyAndWaterUpdate = "EnergyAndWaterUsage"
  val EnergyUsageUpdate = "EnergyUsage"
  val WaterUsageUpdate = "WaterUsage"

  @derive(decoder, encoder)
  sealed trait Usage extends DataUpdate {
    val timestamp: Long
    val usage: Long
  }

  @derive(decoder, encoder)
  case class EnergyUsage(
    usage    : Long,
    timestamp: Long
  ) extends Usage

  object EnergyUsage {
    def empty: EnergyUsage = EnergyUsage(0L, 0L)
  }

  @derive(decoder, encoder)
  case class WaterUsage(
    usage    : Long,
    timestamp: Long
  ) extends Usage

  object WaterUsage {
    def empty: WaterUsage = WaterUsage(0L, 0L)
  }

  @derive(decoder, encoder)
  case class AggregatedUsage(
    energy: EnergyUsage,
    water : WaterUsage
  )

  object AggregatedUsage {
    def empty: AggregatedUsage = AggregatedUsage(EnergyUsage.empty, WaterUsage.empty)
  }

  @derive(decoder, encoder)
  case class TxnRef(
    txnSnapshotOrdinal: SnapshotOrdinal,
    txnHash: String
  )

  object TxnRef {
    def empty: TxnRef = TxnRef(SnapshotOrdinal.MinValue, "0000000000000000000000000000000000000000000000000000000000000000")
  }

  @derive(decoder, encoder)
  case class UsageUpdate(
    address    : Address,
    energyUsage: Option[EnergyUsage],
    waterUsage : Option[WaterUsage]
  ) extends DataUpdate

  @derive(decoder, encoder)
  case class UsageUpdateState(
    updates: List[UpdateUsageTransaction]
  ) extends DataOnChainState

  @derive(decoder, encoder)
  case class DeviceCalculatedState(
    usages       : AggregatedUsage,
    currentTxnRef: TxnRef
  )

  object DeviceCalculatedState {
    def empty: DeviceCalculatedState = DeviceCalculatedState(AggregatedUsage(EnergyUsage.empty, WaterUsage.empty), TxnRef.empty)
  }

  @derive(decoder, encoder)
  case class UsageUpdateCalculatedState(
    devices: Map[Address, DeviceCalculatedState]
  ) extends DataCalculatedState

  @derive(decoder, encoder)
  case class UpdateUsageTransaction(
    owner             : Address,
    transactionType   : String,
    energyUpdateAmount: Long,
    waterUpdateAmount : Long,
    lastTxnOrdinal    : SnapshotOrdinal,
    lastTxnHash       : String
  )

  @derive(decoder, encoder)
  case class AddressTransactionsWithLastRef(txnRef: TxnRef, txns: List[UpdateUsageTransaction])
}
