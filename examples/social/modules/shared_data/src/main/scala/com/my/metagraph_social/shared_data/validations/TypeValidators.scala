package com.my.metagraph_social.shared_data.validations

import cats.syntax.all._
import com.my.metagraph_social.shared_data.errors.Errors._
import com.my.metagraph_social.shared_data.types.States.SocialCalculatedState
import com.my.metagraph_social.shared_data.types.Updates.Subscribe
import org.tessellation.schema.address.Address

object TypeValidators {
  def validateIfPostContentIsGreaterThan200Chars(
    postContent: String
  ): DataApplicationValidationType =
    TooLargePost.whenA(postContent.length > 200)

  def validateIfPostAlreadyExists(
    postId         : String,
    userId         : Address,
    calculatedState: SocialCalculatedState
  ): DataApplicationValidationType =
    PostAlreadyExists.whenA(calculatedState.users.get(userId).exists(_.posts.exists(_.postId == postId)))

  def validateIfPostExists(
    postId         : String,
    userId         : Address,
    calculatedState: SocialCalculatedState
  ): DataApplicationValidationType =
    PostNotExists.unlessA(calculatedState.users.get(userId).exists(_.posts.exists(_.postId == postId)))

  def validateIfSubscriptionUserExists(
    update         : Subscribe,
    calculatedState: SocialCalculatedState
  ): DataApplicationValidationType =
    SubscriptionUserDoesNotExists.whenA(!calculatedState.users.keySet.contains(update.userId))

  def validateIfUserAlreadySubscribed(
    update         : Subscribe,
    userId         : Address,
    calculatedState: SocialCalculatedState
  ): DataApplicationValidationType =
    UserAlreadySubscribed.whenA(calculatedState.users.get(userId).exists(_.subscriptions.contains(update.userId)))

  def validateIfUserIsSubscribingToSelf(
    update: Subscribe,
    userId: Address,
  ): DataApplicationValidationType =
    CannotSubscribeSelf.whenA(update.userId === userId)
}

