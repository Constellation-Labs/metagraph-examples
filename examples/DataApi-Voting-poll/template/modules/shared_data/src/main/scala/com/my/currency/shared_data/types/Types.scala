package com.my.currency.shared_data.types

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.tessellation.currency.dataApplication.{DataCalculatedState, DataOnChainState, DataUpdate}
import org.tessellation.schema.address.Address

object Types {
  @derive(decoder, encoder)
  sealed trait PollUpdate extends DataUpdate

  @derive(decoder, encoder)
  case class CreatePoll(name: String, owner: Address, pollOptions: List[String], startSnapshotOrdinal: Long, endSnapshotOrdinal: Long) extends PollUpdate

  @derive(decoder, encoder)
  case class VoteInPoll(pollId: String, address: Address, option: String) extends PollUpdate

  @derive(decoder, encoder)
  sealed trait PollState {
    val name: String
    val owner: Address
    val pollOptions: Map[String, Long]
    val usersVotes: Map[Address, Map[String, Long]]
    val startSnapshotOrdinal: Long
    val endSnapshotOrdinal: Long
  }

  @derive(decoder, encoder)
  case class Poll(id: String, name: String, owner: Address, pollOptions: Map[String, Long], usersVotes: Map[Address, Map[String, Long]], startSnapshotOrdinal: Long, endSnapshotOrdinal: Long) extends PollState

  @derive(decoder, encoder)
  case class VoteStateOnChain(updates: List[PollUpdate]) extends DataOnChainState

  @derive(decoder, encoder)
  case class VoteCalculatedState(polls: Map[String, Poll]) extends DataCalculatedState
}
