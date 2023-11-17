package com.my.nft_example.shared_data.calculated_state

import cats.effect.IO
import com.my.nft_example.shared_data.types.Types.NFTUpdatesCalculatedState
import eu.timepit.refined.types.numeric.NonNegLong
import io.circe.syntax.EncoderOps
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.hash.Hash

import java.security.MessageDigest

object CalculatedState {

  private var maybeNFTUpdatesCalculatedState: (SnapshotOrdinal, NFTUpdatesCalculatedState) = (
    SnapshotOrdinal(NonNegLong(0L)),
    NFTUpdatesCalculatedState(Map.empty)
  )

  def getCalculatedState: IO[(SnapshotOrdinal, NFTUpdatesCalculatedState)] = {
    IO(maybeNFTUpdatesCalculatedState)
  }

  def setCalculatedState(snapshotOrdinal: SnapshotOrdinal, state: NFTUpdatesCalculatedState): IO[Boolean] = {
    val currentCheckInCalculatedState = maybeNFTUpdatesCalculatedState._2
    val updatedDevices = state.collections.foldLeft(currentCheckInCalculatedState.collections) {
      case (acc, (address, value)) =>
        acc.updated(address, value)
    }

    maybeNFTUpdatesCalculatedState = (
      snapshotOrdinal,
      NFTUpdatesCalculatedState(updatedDevices)
    )

    IO(true)
  }

  private def sha256Hash(input: String): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(input.getBytes("UTF-8"))
    hashBytes.map("%02x".format(_)).mkString
  }

  def hashCalculatedState(state: NFTUpdatesCalculatedState): IO[Hash] = {
    val jsonState = state.asJson.deepDropNullValues.noSpaces
    val hashedState = sha256Hash(jsonState)
    IO(Hash(hashedState))
  }
}
