package com.my.currency.l0.custom_routes

import cats.effect.Async
import cats.implicits.{catsSyntaxApplicativeError, catsSyntaxFlatMapOps, toFlatMapOps}
import com.my.currency.shared_data.calculated_state.CalculatedState.getCalculatedState
import com.my.currency.shared_data.types.Types.Poll
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import eu.timepit.refined.auto._
import org.http4s.{HttpRoutes, Response}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.CORS
import org.tessellation.http.routes.internal.{InternalUrlPrefix, PublicRoutes}
import org.tessellation.schema.address.Address
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

case class CustomRoutes[F[_] : Async]() extends Http4sDsl[F] with PublicRoutes[F] {
  implicit val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

  @derive(decoder, encoder)
  case class PollResponse(id: String, name: String, owner: Address, result: Map[String, Long], startSnapshotOrdinal: Long, endSnapshotOrdinal: Long, status: String)

  private def formatPoll(poll: Poll, lastOrdinal: Long): PollResponse = {
    if (poll.endSnapshotOrdinal < lastOrdinal) {
      PollResponse(poll.id, poll.name, poll.owner, poll.pollOptions, poll.startSnapshotOrdinal, poll.endSnapshotOrdinal, "Closed")
    } else {
      PollResponse(poll.id, poll.name, poll.owner, poll.pollOptions, poll.startSnapshotOrdinal, poll.endSnapshotOrdinal, "Open")
    }
  }

  private def getAllPolls: F[Response[F]] = {
    getCalculatedState
      .flatMap { state =>
        val pollsResponse = state._2.polls.map { case (_, value) => formatPoll(value, state._1.value.value) }
        Ok(pollsResponse)
      }
      .handleErrorWith(e => logger.error(e)(s"An error occured!") >> BadRequest())
  }

  private def getPollById(pollId: String): F[Response[F]] = {
    getCalculatedState
      .flatMap { state =>
        val poll = state._2.polls.get(pollId)
        poll match {
          case Some(value) => Ok(formatPoll(value, state._1.value.value))
          case None => NotFound()
        }
      }
      .handleErrorWith(e => logger.error(e)(s"An error occured!") >> BadRequest())
  }

  private val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "polls" => getAllPolls
    case GET -> Root / "polls" / poolId => getPollById(poolId)
  }

  val public: HttpRoutes[F] =
    CORS
      .policy
      .withAllowCredentials(false)
      .httpRoutes(routes)

  override protected def prefixPath: InternalUrlPrefix = "/"
}
