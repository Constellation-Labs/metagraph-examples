package com.my.currency.shared_data.calculated_state

import cats.Applicative
import cats.effect.kernel.Async
import cats.implicits.{catsSyntaxApplicativeId, toFunctorOps}
import com.my.currency.shared_data.types.Types.VoteCalculatedState
import eu.timepit.refined.types.numeric.NonNegLong
import io.circe.syntax.EncoderOps
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.hash.Hash

import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicReference

object CalculatedState {

  private val maybeVoteCalculatedState: AtomicReference[(SnapshotOrdinal, VoteCalculatedState)] = new AtomicReference(
    (SnapshotOrdinal(NonNegLong(0L)),
      VoteCalculatedState(Map.empty))
  )

  def getCalculatedState[F[_]: Applicative]: F[(SnapshotOrdinal, VoteCalculatedState)] = {
    maybeVoteCalculatedState.get().pure[F]
  }

  def setCalculatedState[F[_] : Async](snapshotOrdinal: SnapshotOrdinal, state: VoteCalculatedState): F[Boolean] = Async[F].delay {
    val currentVoteCalculatedState = maybeVoteCalculatedState.get()._2
    val updatedPolls = state.polls.foldLeft(currentVoteCalculatedState.polls) {
      case (acc, (address, value)) =>
        acc.updated(address, value)
    }

    maybeVoteCalculatedState.set((
      snapshotOrdinal,
      VoteCalculatedState(updatedPolls)
    )
    )
  }.as(true)

  private def sha256Hash(input: String): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(input.getBytes("UTF-8"))
    hashBytes.map("%02x".format(_)).mkString
  }

  def hashCalculatedState[F[_] : Async](state: VoteCalculatedState): F[Hash] = Async[F].delay {
    val jsonState = state.asJson.deepDropNullValues.noSpaces
    val hashedState = sha256Hash(jsonState)
    Hash(hashedState)
  }
}
