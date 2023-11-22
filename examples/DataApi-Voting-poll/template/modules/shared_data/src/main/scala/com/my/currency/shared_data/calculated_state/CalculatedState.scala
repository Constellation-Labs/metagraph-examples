package com.my.currency.shared_data.calculated_state

import cats.Applicative
import cats.effect.kernel.Async
import cats.implicits.{catsSyntaxApplicativeId, toFunctorOps}
import com.my.currency.shared_data.types.Types.VoteCalculatedState
import eu.timepit.refined.types.numeric.NonNegLong
import io.circe.syntax.EncoderOps
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.hash.Hash

import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference

object CalculatedState {

  private val stateRef: AtomicReference[(SnapshotOrdinal, VoteCalculatedState)] = new AtomicReference(
    (SnapshotOrdinal(NonNegLong(0L)),
      VoteCalculatedState(Map.empty))
  )

  def getCalculatedState[F[_]: Applicative]: F[(SnapshotOrdinal, VoteCalculatedState)] = {
    stateRef.get().pure[F]
  }

  def setCalculatedState[F[_] : Async](snapshotOrdinal: SnapshotOrdinal, state: VoteCalculatedState): F[Boolean] = Async[F].delay {
    val currentVoteCalculatedState = stateRef.get()._2
    val updatedPolls = state.polls.foldLeft(currentVoteCalculatedState.polls) {
      case (acc, (address, value)) =>
        acc.updated(address, value)
    }

    stateRef.set((
      snapshotOrdinal,
      VoteCalculatedState(updatedPolls)
    )
    )
  }.as(true)

  def hashCalculatedState[F[_] : Async](state: VoteCalculatedState): F[Hash] = Async[F].delay {
    val jsonState = state.asJson.deepDropNullValues.noSpaces
    Hash.fromBytes(jsonState.getBytes(StandardCharsets.UTF_8))
  }
}
