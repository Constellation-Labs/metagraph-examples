package com.my.nft_fee_transactions.shared_data.calculated_state

import com.my.nft_fee_transactions.shared_data.types.Types._
import eu.timepit.refined.types.all.NonNegLong
import io.constellationnetwork.schema.SnapshotOrdinal

case class CalculatedState(ordinal: SnapshotOrdinal, state: NFTUpdatesCalculatedState)

object CalculatedState {
  def empty: CalculatedState =
    CalculatedState(SnapshotOrdinal(NonNegLong(0L)), NFTUpdatesCalculatedState(Map.empty))
}
