package com.my.water_and_energy_usage.shared_data.calculated_state

import com.my.water_and_energy_usage.shared_data.types.Types.UsageUpdateCalculatedState
import io.constellationnetwork.schema.SnapshotOrdinal

case class CalculatedState(ordinal: SnapshotOrdinal, state: UsageUpdateCalculatedState)

object CalculatedState {
  def empty: CalculatedState =
    CalculatedState(
      SnapshotOrdinal.MinValue,
      UsageUpdateCalculatedState(Map.empty)
    )

}
