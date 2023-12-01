package com.my.nft_example.shared_data.calculated_state

import com.my.nft_example.shared_data.types.Types.NFTUpdatesCalculatedState
import eu.timepit.refined.types.all.NonNegLong
import org.tessellation.schema.SnapshotOrdinal

case class CalculatedState(ordinal: SnapshotOrdinal, state: NFTUpdatesCalculatedState)

object CalculatedState {
  def empty: CalculatedState =
    CalculatedState(SnapshotOrdinal(NonNegLong(0L)), NFTUpdatesCalculatedState(Map.empty))
}
