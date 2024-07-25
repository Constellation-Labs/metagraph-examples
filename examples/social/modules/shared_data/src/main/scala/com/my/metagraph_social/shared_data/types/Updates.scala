package com.my.metagraph_social.shared_data.types

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.tessellation.currency.dataApplication.DataUpdate
import org.tessellation.schema.address.Address

object Updates {
  @derive(decoder, encoder)
  sealed trait SocialUpdate extends DataUpdate

  @derive(decoder, encoder)
  case class CreatePost(content: String) extends SocialUpdate

  @derive(decoder, encoder)
  case class EditPost(postId: String, content: String) extends SocialUpdate

  @derive(decoder, encoder)
  case class DeletePost(postId: String) extends SocialUpdate

  @derive(decoder, encoder)
  case class Subscribe(userId: Address) extends SocialUpdate
}
