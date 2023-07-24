package com.my.currency.shared_data

import cats.data.NonEmptySet
import cats.effect.IO
import cats.implicits.{catsSyntaxApply, catsSyntaxValidatedIdBinCompat0, toFoldableOps, toTraverseOps}
import com.my.currency.shared_data.MainData.{CreatePoll, State, VoteInPoll}
import com.my.currency.shared_data.TypeValidators.{validateIfOptionExists, validateIfPollAlreadyExists, validateIfUserAlreadyVoted, validateIfVotePollExists, validatePollSnapshotInterval, validateProvidedAddress, validateSnapshotCreatePoll, validateWalletBalance}
import com.my.currency.shared_data.Utils.customUpdateSerialization
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.schema.currency.CurrencySnapshotInfo
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.SecurityProvider
import org.tessellation.security.hash.Hash
import org.tessellation.security.signature.signature.SignatureProof

object Validations {
  def createPollValidations(update: CreatePoll, state: State, lastSnapshotOrdinal: Option[SnapshotOrdinal]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validatedCreatePollSnapshot = lastSnapshotOrdinal match {
      case Some(value) => validateSnapshotCreatePoll(value, update)
      case None => ().validNec
    }

    val voteId = Hash.fromBytes(customUpdateSerialization(update))
    val validatedPoll = validateIfPollAlreadyExists(state, voteId.toString)

    IO {
      validatedCreatePollSnapshot.productR(validatedPoll)
    }
  }

  def voteInPollValidations(update: VoteInPoll, state: State, lastSnapshotOrdinal: Option[SnapshotOrdinal],  snapshotInfo: CurrencySnapshotInfo): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validatedSnapshotInterval = lastSnapshotOrdinal match {
      case Some(value) => validatePollSnapshotInterval(value, state, update)
      case None => ().validNec
    }

    val validatedPoll = validateIfVotePollExists(state, update.pollId)

    val validatedOption = validateIfOptionExists(state, update.pollId, update.option)

    val validatedRepeatedVote = validateIfUserAlreadyVoted(state, update.pollId, update.address)

    val validateBalance = validateWalletBalance(snapshotInfo, update.address)

    IO {
      validatedSnapshotInterval.productR(validatedPoll).productR(validatedOption).productR(validatedRepeatedVote).productR(validateBalance)
    }

  }

  def createPollValidationsWithSignature(update: CreatePoll, proofs: NonEmptySet[SignatureProof], state: State)(implicit sp: SecurityProvider[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddress = proofs
      .map(_.id)
      .toList
      .traverse(_.toAddress[IO])
      .map(validateProvidedAddress(_, update.owner))

    val validations = createPollValidations(update, state, None)

    for {
      validatedAddress <- validateAddress
      validatedPoll <- validations
    } yield validatedAddress.productR(validatedPoll)
  }

  def voteInPollValidationsWithSignature(update: VoteInPoll, proofs: NonEmptySet[SignatureProof], state: State, snapshotInfo: CurrencySnapshotInfo)(implicit sp: SecurityProvider[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddress = proofs
      .map(_.id)
      .toList
      .traverse(_.toAddress[IO])
      .map(validateProvidedAddress(_, update.address))

    val validations = voteInPollValidations(update, state, None, snapshotInfo)

    for {
      validatedAddress <- validateAddress
      validatedPoll <- validations
    } yield validatedAddress.productR(validatedPoll)
  }
}

