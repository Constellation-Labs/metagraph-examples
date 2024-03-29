package com.my.voting_poll.shared_data.serializers

import com.my.voting_poll.shared_data.types.Types.{PollUpdate, VoteCalculatedState, VoteStateOnChain}
import io.circe.Encoder
import io.circe.syntax.EncoderOps
import org.tessellation.currency.dataApplication.DataUpdate
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationBlock
import org.tessellation.security.signature.Signed

import java.nio.charset.StandardCharsets

object Serializers {
  private def serialize[A: Encoder](serializableData: A): Array[Byte] =
    serializableData.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)

  def serializeUpdate(update: PollUpdate): Array[Byte] = {
    serialize[PollUpdate](update)
  }

  def serializeState(state: VoteStateOnChain): Array[Byte] = {
    serialize[VoteStateOnChain](state)
  }

  def serializeBlock(state: Signed[DataApplicationBlock])(implicit e: Encoder[DataUpdate]): Array[Byte] = {
    serialize[Signed[DataApplicationBlock]](state)
  }

  def serializeCalculatedState(state: VoteCalculatedState): Array[Byte] = {
    serialize[VoteCalculatedState](state)
  }
}