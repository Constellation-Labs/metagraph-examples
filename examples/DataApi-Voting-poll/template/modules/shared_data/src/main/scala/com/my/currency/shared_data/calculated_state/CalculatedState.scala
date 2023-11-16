package com.my.currency.shared_data.calculated_state

import cats.effect.IO
import com.my.currency.shared_data.types.Types.VoteCalculatedState
import eu.timepit.refined.types.numeric.NonNegLong
import io.circe.syntax.EncoderOps
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.hash.Hash

import java.security.MessageDigest

object CalculatedState {

  private var maybeVoteCalculatedState: (SnapshotOrdinal, VoteCalculatedState) = (
    SnapshotOrdinal(NonNegLong(0L)),
    VoteCalculatedState(Map.empty)
  )

  def getCalculatedState: IO[(SnapshotOrdinal, VoteCalculatedState)] = {
    IO(maybeVoteCalculatedState)
  }

  def setCalculatedState(snapshotOrdinal: SnapshotOrdinal, state: VoteCalculatedState): IO[Boolean] = {
    val currentCheckInCalculatedState = maybeVoteCalculatedState._2
    val updatedDevices = state.polls.foldLeft(currentCheckInCalculatedState.polls) {
      case (acc, (address, value)) =>
        acc.updated(address, value)
    }

    maybeVoteCalculatedState = (
      snapshotOrdinal,
      VoteCalculatedState(updatedDevices)
    )

    IO(true)
  }

  private def sha256Hash(input: String): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(input.getBytes("UTF-8"))
    hashBytes.map("%02x".format(_)).mkString
  }

  def hashCalculatedState(state: VoteCalculatedState): IO[Hash] = {
    val jsonState = state.asJson.deepDropNullValues.noSpaces
    val hashedState = sha256Hash(jsonState)
    IO(Hash(hashedState))
  }
}
