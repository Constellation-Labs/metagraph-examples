package com.my.currency.shared_data.validations

import com.my.currency.shared_data.errors.Errors._
import com.my.currency.shared_data.types.Types.{VoteCalculatedState, CreatePoll, VoteStateOnChain, VoteInPoll}
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.currency.schema.currency.CurrencySnapshotInfo
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.address.Address

object TypeValidators {
  def validateIfPollAlreadyExists(state: DataState[VoteStateOnChain, VoteCalculatedState], pollId: String): DataApplicationValidationType =
    PollAlreadyExists.whenA(state.calculated.polls.contains(pollId))

  def validateIfVotePollExists(state: DataState[VoteStateOnChain, VoteCalculatedState], pollId: String): DataApplicationValidationType =
    PollDoesNotExists.unlessA(state.calculated.polls.contains(pollId))

  def validateIfOptionExists(state: DataState[VoteStateOnChain, VoteCalculatedState], pollId: String, option: String): DataApplicationValidationType =
    InvalidOption.whenA(state.calculated.polls.get(pollId).exists(!_.pollOptions.contains(option)))

  def validateProvidedAddress(proofAddresses: List[Address], address: Address): DataApplicationValidationType =
    InvalidAddress.unlessA(proofAddresses.contains(address))

  def validateIfUserAlreadyVoted(state: DataState[VoteStateOnChain, VoteCalculatedState], pollId: String, address: Address): DataApplicationValidationType =
    RepeatedVote.whenA(state.calculated.polls.get(pollId).map(_.usersVotes).exists(_.contains(address)))

  def validateWalletBalance(snapshotInfo: CurrencySnapshotInfo, walletAddress: Address): DataApplicationValidationType =
    NotEnoughWalletBalance.unlessA(snapshotInfo.balances.get(walletAddress).exists(_.value.value > 0L))

  def validateSnapshotCreatePoll(snapshotOrdinal: SnapshotOrdinal, update: CreatePoll): DataApplicationValidationType =
    InvalidEndSnapshot.whenA(update.endSnapshotOrdinal < snapshotOrdinal.value.value)

  def validatePollSnapshotInterval(lastSnapshotOrdinal: SnapshotOrdinal, state: DataState[VoteStateOnChain, VoteCalculatedState], voteInPoll: VoteInPoll): DataApplicationValidationType =
    state.calculated.polls
      .get(voteInPoll.pollId)
      .map(value => ClosedPool.whenA(value.endSnapshotOrdinal < lastSnapshotOrdinal.value.value))
      .getOrElse(PollDoesNotExists.invalid)
}

