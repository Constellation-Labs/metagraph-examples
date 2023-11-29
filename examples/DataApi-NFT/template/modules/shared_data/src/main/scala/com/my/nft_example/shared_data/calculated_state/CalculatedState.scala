package com.my.nft_example.shared_data.calculated_state

import cats.Applicative
import cats.effect.Async
import cats.implicits.{catsSyntaxApplicativeId, toFunctorOps}
import com.my.nft_example.shared_data.types.Types.NFTUpdatesCalculatedState
import eu.timepit.refined.types.numeric.NonNegLong
import io.circe.syntax.EncoderOps
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.hash.Hash

import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference

object CalculatedState {

  private val maybeNFTUpdatesCalculatedState: AtomicReference[(SnapshotOrdinal, NFTUpdatesCalculatedState)] = new AtomicReference(
    (SnapshotOrdinal(NonNegLong(0L)),
      NFTUpdatesCalculatedState(Map.empty))
  )

  def getCalculatedState[F[_] : Applicative]: F[(SnapshotOrdinal, NFTUpdatesCalculatedState)] = {
    maybeNFTUpdatesCalculatedState.get().pure[F]
  }

  def setCalculatedState[F[_] : Async](
    snapshotOrdinal: SnapshotOrdinal,
    state          : NFTUpdatesCalculatedState
  ): F[Boolean] = Async[F].delay {
    val currentCheckInCalculatedState = maybeNFTUpdatesCalculatedState.get()._2
    val updatedDevices = state.collections.foldLeft(currentCheckInCalculatedState.collections) {
      case (acc, (address, value)) =>
        acc.updated(address, value)
    }

    maybeNFTUpdatesCalculatedState.set((
      snapshotOrdinal,
      NFTUpdatesCalculatedState(updatedDevices)
    ))
  }.as(true)

  def hashCalculatedState[F[_] : Async](
    state: NFTUpdatesCalculatedState
  ): F[Hash] = Async[F].delay {
    val jsonState = state.asJson.deepDropNullValues.noSpaces
    Hash.fromBytes(jsonState.getBytes(StandardCharsets.UTF_8))
  }
}
