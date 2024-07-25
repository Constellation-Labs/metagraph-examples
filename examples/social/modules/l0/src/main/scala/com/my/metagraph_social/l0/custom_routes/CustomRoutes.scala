package com.my.metagraph_social.l0.custom_routes

import cats.effect.Async
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.my.metagraph_social.shared_data.calculated_state.CalculatedStateService
import com.my.metagraph_social.shared_data.types.States.UserPost
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import eu.timepit.refined.auto._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.CORS
import org.http4s.{HttpRoutes, Response}
import org.tessellation.ext.http4s.AddressVar
import org.tessellation.routes.internal.{InternalUrlPrefix, PublicRoutes}
import org.tessellation.schema.address.Address
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

case class CustomRoutes[F[_] : Async](calculatedStateService: CalculatedStateService[F]) extends Http4sDsl[F] with PublicRoutes[F] {
  implicit val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

  @derive(decoder, encoder)
  case class UserFeedPost(userId: Address, userPost: UserPost)

  private def getAllUsersWithPosts: F[Response[F]] = {
    calculatedStateService.get
      .map(_.state)
      .map { state =>
        state.users.filter { info =>
            val (_, userInfo) = info
            userInfo.posts.nonEmpty
          }
          .keySet
      }
      .flatMap { existingUsers =>
        Ok(existingUsers)
      }
  }

  private def getAllPosts: F[Response[F]] = {
    calculatedStateService.get
      .map(_.state)
      .map { state =>
        state.users
          .map { info =>
            val (address, userInfo) = info
            userInfo.posts.map(UserFeedPost(address, _))
          }
          .toList
          .flatten

      }
      .flatMap { allPosts =>
        Ok(allPosts)
      }
  }

  private def getUserPosts(userId: Address): F[Response[F]] = {
    calculatedStateService.get
      .map(_.state)
      .map { state => state.users.get(userId) }
      .flatMap { maybeUserInfo =>
        maybeUserInfo.fold(NotFound())(userInfo => Ok(userInfo.posts))
      }
  }

  private def getUserSubscriptions(userId: Address): F[Response[F]] = {
    calculatedStateService.get
      .map(_.state)
      .map { state => state.users.get(userId) }
      .flatMap { maybeUserInfo =>
        maybeUserInfo.fold(NotFound())(userInfo => Ok(userInfo.subscriptions))
      }
  }

  private def getUserFeed(userId: Address): F[Response[F]] = {
    for {
      calculatedState <- calculatedStateService.get
      userInformation = calculatedState.state.users
      subscriptions = userInformation
        .get(userId)
        .map(_.subscriptions)
        .getOrElse(List.empty[Address])
      responsePosts = subscriptions.flatMap { subscriptionAddress =>
        userInformation
          .get(subscriptionAddress)
          .map(_.posts.map(UserFeedPost(subscriptionAddress, _)))
          .getOrElse(List.empty[UserFeedPost])
      }
      response <- Ok(responsePosts)
    } yield response
  }

  private val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "users" => getAllUsersWithPosts
    case GET -> Root / "posts" => getAllPosts
    case GET -> Root / "users" / AddressVar(userId) / "posts" => getUserPosts(userId)
    case GET -> Root / "users" / AddressVar(userId) / "subscriptions" => getUserSubscriptions(userId)
    case GET -> Root / "users" / AddressVar(userId) / "feed" => getUserFeed(userId)
  }

  val public: HttpRoutes[F] =
    CORS
      .policy
      .withAllowCredentials(false)
      .httpRoutes(routes)

  override protected def prefixPath: InternalUrlPrefix = "/"
}
