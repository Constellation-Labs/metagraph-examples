package com.my.metagraph_social.shared_data.combiners

import cats.effect.Async
import cats.syntax.all._
import com.my.metagraph_social.shared_data.Utils.getFirstAddressFromProofs
import com.my.metagraph_social.shared_data.types.States._
import com.my.metagraph_social.shared_data.types.Updates._
import io.constellationnetwork.currency.dataApplication.DataState
import io.constellationnetwork.json.JsonSerializer
import io.constellationnetwork.schema.SnapshotOrdinal
import io.constellationnetwork.security.SecurityProvider
import io.constellationnetwork.security.hash.Hash
import io.constellationnetwork.security.signature.Signed
import monocle.Monocle.toAppliedFocusOps

import java.time.LocalDateTime

object Combiners {
  def combineCreatePost[F[_] : Async : SecurityProvider : JsonSerializer](
    signedUpdate  : Signed[CreatePost],
    state         : DataState[SocialOnChainState, SocialCalculatedState],
    currentOrdinal: SnapshotOrdinal
  ): F[DataState[SocialOnChainState, SocialCalculatedState]] = {
    val update = signedUpdate.value
    for {
      updateBytes <- JsonSerializer[F].serialize[SocialUpdate](update)
      postId = Hash.fromBytes(updateBytes).toString
      updateAddress <- getFirstAddressFromProofs(signedUpdate.proofs)
      currentUserInformation = state.calculated.users
        .getOrElse(updateAddress, UserInformation.empty(updateAddress))
      userPost = UserPost(postId, update.content, currentOrdinal, LocalDateTime.now())
      userInformationUpdated = currentUserInformation
        .focus(_.posts)
        .modify(existingPosts => userPost +: existingPosts)

      onChainStateUpdated = SocialOnChainState(state.onChain.updates :+ signedUpdate.value)
      calculatedStateUpdated = state.calculated
        .focus(_.users)
        .modify(_.updated(updateAddress, userInformationUpdated))
    } yield DataState(onChainStateUpdated, calculatedStateUpdated)
  }

  def combineEditPost[F[_] : Async : SecurityProvider](
    signedUpdate  : Signed[EditPost],
    state         : DataState[SocialOnChainState, SocialCalculatedState],
    currentOrdinal: SnapshotOrdinal
  ): F[DataState[SocialOnChainState, SocialCalculatedState]] = {
    val update = signedUpdate.value
    for {
      updateAddress <- getFirstAddressFromProofs(signedUpdate.proofs)
      currentUserInformation <- state.calculated.users
        .get(updateAddress)
        .toOptionT
        .getOrRaise(new Exception(s"Could not get user information"))
      currentPost <- currentUserInformation.posts
        .find(_.postId == update.postId)
        .toOptionT
        .getOrRaise(new Exception(s"Post does not exists"))
      updatedUserPost = currentPost
        .focus(_.content)
        .replace(update.content)
        .focus(_.ordinal)
        .replace(currentOrdinal)
        .focus(_.postTime)
        .replace(LocalDateTime.now())
      userInformationUpdated = currentUserInformation
        .focus(_.posts)
        .modify(_.map(post => if (post.postId == update.postId) updatedUserPost else post))

      onChainStateUpdated = SocialOnChainState(state.onChain.updates :+ signedUpdate.value)
      calculatedStateUpdated = state.calculated
        .focus(_.users)
        .modify(_.updated(updateAddress, userInformationUpdated))
    } yield DataState(onChainStateUpdated, calculatedStateUpdated)
  }

  def combineDeletePost[F[_] : Async : SecurityProvider](
    signedUpdate: Signed[DeletePost],
    state       : DataState[SocialOnChainState, SocialCalculatedState]
  ): F[DataState[SocialOnChainState, SocialCalculatedState]] = {
    val update = signedUpdate.value
    for {
      updateAddress <- getFirstAddressFromProofs(signedUpdate.proofs)
      currentUserInformation <- state.calculated.users
        .get(updateAddress)
        .toOptionT
        .getOrRaise(new Exception(s"Could not get user information"))
      userInformationUpdated = currentUserInformation
        .focus(_.posts)
        .modify(_.filter(post => post.postId != update.postId))

      onChainStateUpdated = SocialOnChainState(state.onChain.updates :+ signedUpdate.value)
      calculatedStateUpdated = state.calculated
        .focus(_.users)
        .modify(_.updated(updateAddress, userInformationUpdated))
    } yield DataState(onChainStateUpdated, calculatedStateUpdated)
  }

  def combineSubscribe[F[_] : Async : SecurityProvider](
    signedUpdate: Signed[Subscribe],
    state       : DataState[SocialOnChainState, SocialCalculatedState],
  ): F[DataState[SocialOnChainState, SocialCalculatedState]] = {
    val update = signedUpdate.value
    for {
      updateAddress <- getFirstAddressFromProofs(signedUpdate.proofs)
      currentUserInformation = state.calculated.users
        .getOrElse(updateAddress, UserInformation.empty(updateAddress))
      userInformationUpdated = currentUserInformation
        .focus(_.subscriptions)
        .modify(existingSubscriptions => update.userId +: existingSubscriptions)

      onChainStateUpdated = SocialOnChainState(state.onChain.updates :+ signedUpdate.value)
      calculatedStateUpdated = state.calculated
        .focus(_.users)
        .modify(_.updated(updateAddress, userInformationUpdated))
    } yield DataState(onChainStateUpdated, calculatedStateUpdated)
  }
}