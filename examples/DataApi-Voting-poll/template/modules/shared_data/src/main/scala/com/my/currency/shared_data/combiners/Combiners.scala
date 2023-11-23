package com.my.currency.shared_data.combiners

import com.my.currency.shared_data.serializers.Serializers
import com.my.currency.shared_data.types.Types.{CreatePoll, Poll, VoteCalculatedState, VoteInPoll, VoteStateOnChain}
import monocle.Monocle.toAppliedFocusOps
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.currency.schema.currency.CurrencySnapshotInfo
import org.tessellation.security.hash.Hash

object Combiners {
  def combineCreatePoll(createPoll: CreatePoll, state: DataState[VoteStateOnChain, VoteCalculatedState]): DataState[VoteStateOnChain, VoteCalculatedState] = {
    val pollId = Hash.fromBytes(Serializers.serializeUpdate(createPoll)).toString
    val pollOptions = createPoll.pollOptions.flatMap(option => Map(option -> 0L)).toMap
    val newState = Poll(pollId, createPoll.name, createPoll.owner, pollOptions, Map.empty, createPoll.startSnapshotOrdinal, createPoll.endSnapshotOrdinal)

    val newOnChain = VoteStateOnChain(state.onChain.updates :+ createPoll)
    val newCalculatedState = state.calculated.focus(_.polls).modify(_.updated(pollId, newState))

    DataState(newOnChain, newCalculatedState)
  }

  def combineVoteInPoll(voteInPoll: VoteInPoll, state: DataState[VoteStateOnChain, VoteCalculatedState], snapshotInfo: CurrencySnapshotInfo): DataState[VoteStateOnChain, VoteCalculatedState] = {
    val currentState = state.calculated.polls(voteInPoll.pollId)
    val currentOptionNumber = currentState.pollOptions(voteInPoll.option)

    val addressBalance = snapshotInfo.balances.get(voteInPoll.address)

    addressBalance match {
      case Some(balance) =>
        val votingAmount = balance.value.value
        val newState = currentState
          .focus(_.pollOptions)
          .modify(_.updated(voteInPoll.option, currentOptionNumber + votingAmount))
          .focus(_.usersVotes)
          .modify(_.updated(voteInPoll.address, Map(voteInPoll.option -> votingAmount)))

        val newOnChain = VoteStateOnChain(state.onChain.updates :+ voteInPoll)
        val newCalculatedState = state.calculated.focus(_.polls).modify(_.updated(voteInPoll.pollId, newState))
        DataState(newOnChain, newCalculatedState)

      case None => state
    }
  }
}