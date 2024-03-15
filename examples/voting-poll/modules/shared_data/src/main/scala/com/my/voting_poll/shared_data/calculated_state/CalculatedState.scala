package com.my.voting_poll.shared_data.calculated_state

import com.my.voting_poll.shared_data.types.Types.VoteCalculatedState
import eu.timepit.refined.types.all.NonNegLong
import org.tessellation.schema.SnapshotOrdinal

case class CalculatedState(ordinal: SnapshotOrdinal, state: VoteCalculatedState)

object CalculatedState {
  def empty: CalculatedState =
    CalculatedState(SnapshotOrdinal(NonNegLong.MinValue), VoteCalculatedState(Map.empty))
}