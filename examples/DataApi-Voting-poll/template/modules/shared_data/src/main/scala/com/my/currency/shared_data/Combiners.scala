package com.my.currency.shared_data

import com.my.currency.shared_data.MainData.{CreatePoll, Poll, State, VoteInPoll}
import com.my.currency.shared_data.Utils.customUpdateSerialization
import monocle.Monocle.toAppliedFocusOps
import org.tessellation.security.hash.Hash

object Combiners {
  def combineCreatePoll(poll: CreatePoll, acc: State): State = {
    val pollId = Hash.fromBytes(customUpdateSerialization(poll)).toString
    val pollOptions = poll.pollOptions.flatMap(option => Map(option -> 0L)).toMap
    val newState = Poll(pollId, poll.name, poll.owner, pollOptions, Map.empty, poll.startSnapshotOrdinal, poll.endSnapshotOrdinal)

    acc.focus(_.polls).modify(_.updated(pollId, newState))
  }

  def combineVoteInPoll(voteInPoll: VoteInPoll, acc: State): State = {
    val currentState = acc.polls(voteInPoll.pollId)
    val currentOptionNumber = currentState.pollOptions(voteInPoll.option)

    val newState = currentState
      .focus(_.pollOptions)
      .modify(_.updated(voteInPoll.option, currentOptionNumber + 1))
      .focus(_.usersVotes)
      .modify(_.updated(voteInPoll.address, Map(voteInPoll.option -> 1)))

    acc.focus(_.polls).modify(_.updated(voteInPoll.pollId, newState))
  }
}