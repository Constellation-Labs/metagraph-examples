package com.my.metagraph_social.shared_data.calculated_state

import com.my.metagraph_social.shared_data.types.States.SocialCalculatedState
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import eu.timepit.refined.types.all.NonNegLong
import org.tessellation.schema.SnapshotOrdinal

@derive(decoder, encoder)
case class CalculatedState(ordinal: SnapshotOrdinal, state: SocialCalculatedState)

object CalculatedState {
  def empty: CalculatedState =
    CalculatedState(SnapshotOrdinal(NonNegLong.MinValue), SocialCalculatedState(Map.empty))
}