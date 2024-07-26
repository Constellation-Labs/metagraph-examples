package com.my.metagraph_social.shared_data.types

import com.my.metagraph_social.shared_data.types.Updates.SocialUpdate
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.tessellation.currency.dataApplication.{DataCalculatedState, DataOnChainState}
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.address.Address

import java.time.LocalDateTime

object States {
  @derive(decoder, encoder)
  case class UserPost(postId: String, content: String, ordinal: SnapshotOrdinal, postTime: LocalDateTime)

  @derive(decoder, encoder)
  case class UserInformation(
    userId       : Address,
    posts        : List[UserPost],
    subscriptions: List[Address]
  )

  object UserInformation {
    def empty(userId: Address): UserInformation = UserInformation(userId, List.empty, List.empty)
  }

  @derive(decoder, encoder)
  case class SocialOnChainState(updates: List[SocialUpdate]) extends DataOnChainState

  @derive(decoder, encoder)
  case class SocialCalculatedState(users: Map[Address, UserInformation]) extends DataCalculatedState
}
