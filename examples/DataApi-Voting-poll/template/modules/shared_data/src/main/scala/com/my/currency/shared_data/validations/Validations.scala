package com.my.currency.shared_data.validations

import cats.data.NonEmptySet
import cats.effect.Async
import cats.implicits.{catsSyntaxApply, catsSyntaxOptionId, toFlatMapOps, toFoldableOps, toFunctorOps, toTraverseOps}
import com.my.currency.shared_data.errors.Errors.valid
import com.my.currency.shared_data.serializers.Serializers
import com.my.currency.shared_data.types.Types.{CreatePoll, VoteCalculatedState, VoteInPoll, VoteStateOnChain}
import com.my.currency.shared_data.validations.TypeValidators._
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.schema.currency.CurrencySnapshotInfo
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.address.Address
import org.tessellation.security.SecurityProvider
import org.tessellation.security.hash.Hash
import org.tessellation.security.signature.signature.SignatureProof

object Validations {
  def createPollValidations[F[_] : Async](update: CreatePoll, maybeState: Option[DataState[VoteStateOnChain, VoteCalculatedState]], lastSnapshotOrdinal: Option[SnapshotOrdinal]): F[DataApplicationValidationErrorOr[Unit]] = Async[F].delay {
    val validatedCreatePollSnapshot = lastSnapshotOrdinal match {
      case Some(value) => validateSnapshotCreatePoll(value, update)
      case None => valid
    }

    maybeState match {
      case Some(state) =>
        val voteId = Hash.fromBytes(Serializers.serializeUpdate(update))
        val validatedPoll = validateIfPollAlreadyExists(state, voteId.toString)
        validatedCreatePollSnapshot.productR(validatedPoll)
      case None => validatedCreatePollSnapshot
    }
  }

  def voteInPollValidations[F[_] : Async](update: VoteInPoll, maybeState: Option[DataState[VoteStateOnChain, VoteCalculatedState]], lastSnapshotOrdinal: Option[SnapshotOrdinal], snapshotInfo: CurrencySnapshotInfo): F[DataApplicationValidationErrorOr[Unit]] = Async[F].delay {
    val validateBalance = validateWalletBalance(snapshotInfo, update.address)
    maybeState match {
      case Some(state) =>
        val validatedSnapshotInterval = lastSnapshotOrdinal match {
          case Some(value) => validatePollSnapshotInterval(value, state, update)
          case None => valid
        }

        val validatedPoll = validateIfVotePollExists(state, update.pollId)

        val validatedOption = validateIfOptionExists(state, update.pollId, update.option)

        val validatedRepeatedVote = validateIfUserAlreadyVoted(state, update.pollId, update.address)

        validatedSnapshotInterval
          .productR(validatedPoll)
          .productR(validatedOption)
          .productR(validatedRepeatedVote)
          .productR(validateBalance)

      case None => validateBalance

    }
  }

  private def extractAddresses[F[_] : Async : SecurityProvider](proofs: NonEmptySet[SignatureProof]): F[List[Address]] = {
    proofs
      .map(_.id)
      .toList
      .traverse(_.toAddress[F])
  }

  def createPollValidationsWithSignature[F[_] : Async](update: CreatePoll, proofs: NonEmptySet[SignatureProof], state: DataState[VoteStateOnChain, VoteCalculatedState])(implicit sp: SecurityProvider[F]): F[DataApplicationValidationErrorOr[Unit]] = {
    for {
      addresses <- extractAddresses(proofs)
      validatedAddress = validateProvidedAddress(addresses, update.owner)
      validatedPoll <- createPollValidations(update, state.some, None)
    } yield validatedAddress.productR(validatedPoll)
  }

  def voteInPollValidationsWithSignature[F[_] : Async](update: VoteInPoll, proofs: NonEmptySet[SignatureProof], state: DataState[VoteStateOnChain, VoteCalculatedState], snapshotInfo: CurrencySnapshotInfo)(implicit sp: SecurityProvider[F]): F[DataApplicationValidationErrorOr[Unit]] = {
    for {
      addresses <- extractAddresses(proofs)
      validatedAddress = validateProvidedAddress(addresses, update.address)
      validatedPoll <- voteInPollValidations(update, state.some, None, snapshotInfo)
    } yield validatedAddress.productR(validatedPoll)
  }
}

