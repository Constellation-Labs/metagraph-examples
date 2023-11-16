package com.my.currency.shared_data.validations

import cats.implicits.catsSyntaxValidatedIdBinCompat0
import com.my.currency.shared_data.errors.Errors._
import com.my.currency.shared_data.types.Types.{VoteCalculatedState, CreatePoll, VoteStateOnChain, VoteInPoll}
import org.tessellation.currency.dataApplication.{DataApplicationValidationError, DataState}
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.schema.currency.CurrencySnapshotInfo
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.address.Address

object TypeValidators {
  def validateIfPollAlreadyExists(state: DataState[VoteStateOnChain, VoteCalculatedState], pollId: String): DataApplicationValidationErrorOr[Unit] = {
    val maybeState = state.calculated.polls.get(pollId)
    maybeState match {
      case Some(_) => PollAlreadyExists.asInstanceOf[DataApplicationValidationError].invalidNec
      case None => ().validNec
    }
  }

  def validateIfVotePollExists(state: DataState[VoteStateOnChain, VoteCalculatedState], pollId: String): DataApplicationValidationErrorOr[Unit] = {
    val maybeState = state.calculated.polls.get(pollId)
    maybeState match {
      case Some(_) => ().validNec
      case None => PollDoesNotExists.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }

  def validateIfOptionExists(state: DataState[VoteStateOnChain, VoteCalculatedState], pollId: String, option: String): DataApplicationValidationErrorOr[Unit] = {
    val maybeState = state.calculated.polls.get(pollId)
    maybeState match {
      case Some(value) =>
        val pollValidOptions = value.pollOptions
        pollValidOptions.get(option) match {
          case Some(_) => ().validNec
          case None => InvalidOption.asInstanceOf[DataApplicationValidationError].invalidNec
        }
      case None => InvalidOption.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }

  def validateProvidedAddress(proofAddresses: List[Address], address: Address): DataApplicationValidationErrorOr[Unit] = {
    if (proofAddresses.contains(address)) {
      ().validNec
    } else {
      InvalidAddress.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }

  def validateIfUserAlreadyVoted(state: DataState[VoteStateOnChain, VoteCalculatedState], pollId: String, address: Address): DataApplicationValidationErrorOr[Unit] = {
    val maybeState = state.calculated.polls.get(pollId)
    maybeState match {
      case Some(value) =>
        val pollVotes = value.usersVotes
        pollVotes.get(address) match {
          case Some(_) => RepeatedVote.asInstanceOf[DataApplicationValidationError].invalidNec
          case None => ().validNec
        }
      case None => ().validNec
    }
  }

  def validateWalletBalance(snapshotInfo: CurrencySnapshotInfo, walletAddress: Address): DataApplicationValidationErrorOr[Unit] = {
    val walletBalance = snapshotInfo.balances.get(walletAddress)
    walletBalance match {
      case Some(balance) =>
        if (balance.value.value > 0L) {
          ().validNec
        } else {
          NotEnoughWalletBalance.asInstanceOf[DataApplicationValidationError].invalidNec
        }
      case None => NotEnoughWalletBalance.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }

  def validateSnapshotCreatePoll(snapshotOrdinal: SnapshotOrdinal, update: CreatePoll): DataApplicationValidationErrorOr[Unit] = {
    if (update.endSnapshotOrdinal < snapshotOrdinal.value.value) {
      InvalidEndSnapshot.asInstanceOf[DataApplicationValidationError].invalidNec
    } else {
      ().validNec
    }
  }

  def validatePollSnapshotInterval(lastSnapshotOrdinal: SnapshotOrdinal, state: DataState[VoteStateOnChain, VoteCalculatedState], voteInPoll: VoteInPoll): DataApplicationValidationErrorOr[Unit] = {
    val poll = state.calculated.polls.get(voteInPoll.pollId)
    poll match {
      case Some(value) =>
        if (value.endSnapshotOrdinal < lastSnapshotOrdinal.value.value) {
          ClosedPool.asInstanceOf[DataApplicationValidationError].invalidNec
        } else {
          ().validNec
        }
      case None => PollDoesNotExists.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }
}

