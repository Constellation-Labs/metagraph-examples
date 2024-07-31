package com.my.metagraph_social.shared_data

import cats.data.NonEmptyList
import cats.effect.Async
import cats.syntax.all._
import com.my.metagraph_social.shared_data.Utils.getLastCurrencySnapshotOrdinal
import com.my.metagraph_social.shared_data.combiners.Combiners.{combineCreatePost, combineDeletePost, combineEditPost, combineSubscribe}
import com.my.metagraph_social.shared_data.types.States.{SocialCalculatedState, SocialOnChainState}
import com.my.metagraph_social.shared_data.types.Updates._
import com.my.metagraph_social.shared_data.validations.Validations._
import eu.timepit.refined.types.numeric.NonNegLong
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataState, L0NodeContext}
import org.tessellation.json.JsonSerializer
import org.tessellation.security.SecurityProvider
import org.tessellation.security.signature.Signed
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object LifecycleSharedFunctions {

  def logger[F[_] : Async]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromName[F]("LifecycleSharedFunctions")

  def validateUpdate(update: SocialUpdate): DataApplicationValidationErrorOr[Unit] =
    update match {
      case createPost: CreatePost => createPostValidationL1(createPost)
      case editPost: EditPost => editPostValidationL1(editPost)
      case deletePost: DeletePost => deletePostValidationL1(deletePost)
      case subscribe: Subscribe => subscriptionValidationL1(subscribe)
    }

  def validateData[F[_] : Async : JsonSerializer](
    state  : DataState[SocialOnChainState, SocialCalculatedState],
    updates: NonEmptyList[Signed[SocialUpdate]]
  )(implicit securityProvider: SecurityProvider[F]): F[DataApplicationValidationErrorOr[Unit]] = {
    updates.traverse { signedUpdate =>
      signedUpdate.value match {
        case _: CreatePost =>
          val signedCreatePost = signedUpdate.asInstanceOf[Signed[CreatePost]]
          createPostValidationL0(signedCreatePost, state.calculated)
        case _: EditPost =>
          val signedEditPost = signedUpdate.asInstanceOf[Signed[EditPost]]
          editPostValidationL0(signedEditPost, state.calculated)
        case _: DeletePost =>
          val signedDeletePost = signedUpdate.asInstanceOf[Signed[DeletePost]]
          deletePostValidationL0(signedDeletePost, state.calculated)
        case _: Subscribe =>
          val signedSubscribe = signedUpdate.asInstanceOf[Signed[Subscribe]]
          subscriptionValidationL0(signedSubscribe, state.calculated)
      }
    }.map(_.reduce)
  }

  def combine[F[_] : Async : JsonSerializer](
    state  : DataState[SocialOnChainState, SocialCalculatedState],
    updates: List[Signed[SocialUpdate]]
  )(implicit context: L0NodeContext[F]): F[DataState[SocialOnChainState, SocialCalculatedState]] = {
    val newStateF = DataState(SocialOnChainState(List.empty), state.calculated).pure

    if (updates.isEmpty) {
      logger.info("Snapshot without any update, updating the state to empty updates") >>
        newStateF
    } else {
      getLastCurrencySnapshotOrdinal(Left(context)).flatMap {
        case None =>
          logger.warn("Could not get lastMetagraphIncrementalSnapshotInfo, keeping current state").as(state)
        case Some(lastSnapshotOrdinal) =>
          newStateF
            .flatMap(newState => {
              updates.foldLeftM(newState) { (acc, signedUpdate) =>
                implicit val securityProvider: SecurityProvider[F] = context.securityProvider
                val currentOrdinal = lastSnapshotOrdinal.plus(NonNegLong.unsafeFrom(1L))

                signedUpdate.value match {
                  case _: CreatePost =>
                    val signedCreatePost = signedUpdate.asInstanceOf[Signed[CreatePost]]
                    combineCreatePost(signedCreatePost, acc, currentOrdinal)
                  case _: EditPost =>
                    val signedEditPost = signedUpdate.asInstanceOf[Signed[EditPost]]
                    combineEditPost(signedEditPost, acc, currentOrdinal)
                  case _: DeletePost =>
                    val signedDeletePost = signedUpdate.asInstanceOf[Signed[DeletePost]]
                    combineDeletePost(signedDeletePost, acc)
                  case _: Subscribe =>
                    val signedSubscribe = signedUpdate.asInstanceOf[Signed[Subscribe]]
                    combineSubscribe(signedSubscribe, acc)
                }

              }
            })
      }
    }
  }
}