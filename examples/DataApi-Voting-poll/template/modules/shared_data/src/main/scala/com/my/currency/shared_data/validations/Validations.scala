package com.my.currency.shared_data.validations

import cats.Applicative
import cats.data.NonEmptySet
import cats.effect.Async
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxApply, catsSyntaxOptionId, catsSyntaxValidatedIdBinCompat0, toFlatMapOps, toFoldableOps, toFunctorOps, toTraverseOps}
import com.my.currency.shared_data.serializers.Serializers
import com.my.currency.shared_data.types.Types.{CreatePoll, VoteCalculatedState, VoteInPoll, VoteStateOnChain}
import com.my.currency.shared_data.validations.TypeValidators._
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.schema.currency.CurrencySnapshotInfo
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.SecurityProvider
import org.tessellation.security.hash.Hash
import org.tessellation.security.signature.signature.SignatureProof

object Validations {
  def createPollValidations[F[_] : Applicative](update: CreatePoll, maybeState: Option[DataState[VoteStateOnChain, VoteCalculatedState]], lastSnapshotOrdinal: Option[SnapshotOrdinal]): F[DataApplicationValidationErrorOr[Unit]] = {
    val validatedCreatePollSnapshot = lastSnapshotOrdinal match {
      case Some(value) => validateSnapshotCreatePoll(value, update)
      case None => ().validNec
    }

    maybeState match {
      case Some(state) =>
        val voteId = Hash.fromBytes(Serializers.serializeUpdate(update))
        val validatedPoll = validateIfPollAlreadyExists(state, voteId.toString)
        validatedCreatePollSnapshot.productR(validatedPoll).pure
      case None => validatedCreatePollSnapshot.pure
    }
  }

  def voteInPollValidations[F[_]: Applicative](update: VoteInPoll, maybeState: Option[DataState[VoteStateOnChain, VoteCalculatedState]], lastSnapshotOrdinal: Option[SnapshotOrdinal], snapshotInfo: CurrencySnapshotInfo): F[DataApplicationValidationErrorOr[Unit]] = {
    val validateBalance = validateWalletBalance(snapshotInfo, update.address)
    maybeState match {
      case Some(state) =>
        val validatedSnapshotInterval = lastSnapshotOrdinal match {
          case Some(value) => validatePollSnapshotInterval(value, state, update)
          case None => ().validNec
        }

        val validatedPoll = validateIfVotePollExists(state, update.pollId)

        val validatedOption = validateIfOptionExists(state, update.pollId, update.option)

        val validatedRepeatedVote = validateIfUserAlreadyVoted(state, update.pollId, update.address)

        validatedSnapshotInterval.productR(validatedPoll).productR(validatedOption).productR(validatedRepeatedVote).productR(validateBalance).pure

      case None => validateBalance.pure

    }
  }

  def createPollValidationsWithSignature[F[_] : Async](update: CreatePoll, proofs: NonEmptySet[SignatureProof], state: DataState[VoteStateOnChain, VoteCalculatedState])(implicit sp: SecurityProvider[F]): F[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddressF = proofs
      .map(_.id)
      .toList
      .traverse(_.toAddress[F])
      .map(validateProvidedAddress(_, update.owner))

    val validations = createPollValidations(update, state.some, None)

    for {
      validatedAddress <- validateAddressF
      validatedPoll <- validations
    } yield validatedAddress.productR(validatedPoll)
  }

  def voteInPollValidationsWithSignature[F[_] : Async](update: VoteInPoll, proofs: NonEmptySet[SignatureProof], state: DataState[VoteStateOnChain, VoteCalculatedState], snapshotInfo: CurrencySnapshotInfo)(implicit sp: SecurityProvider[F]): F[DataApplicationValidationErrorOr[Unit]] = {
    val validateAddress = proofs
      .map(_.id)
      .toList
      .traverse(_.toAddress[F])
      .map(validateProvidedAddress(_, update.address))

    val validations = voteInPollValidations(update, state.some, None, snapshotInfo)

    for {
      validatedAddress <- validateAddress
      validatedPoll <- validations
    } yield validatedAddress.productR(validatedPoll)
  }
}

