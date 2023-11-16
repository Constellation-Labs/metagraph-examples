package com.my.currency.l0.custom_routes

import cats.effect.IO
import com.my.currency.shared_data.calculated_state.CalculatedState.getCalculatedState
import com.my.currency.shared_data.types.Types.Poll
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.tessellation.schema.address.Address

object CustomRoutes {
  @derive(decoder, encoder)
  case class PollResponse(id: String, name: String, owner: Address, result: Map[String, Long], startSnapshotOrdinal: Long, endSnapshotOrdinal: Long, status: String)

  private def formatPoll(poll: Poll, lastOrdinal: Long): PollResponse = {
    if (poll.endSnapshotOrdinal < lastOrdinal) {
      PollResponse(poll.id, poll.name, poll.owner, poll.pollOptions, poll.startSnapshotOrdinal, poll.endSnapshotOrdinal, "Closed")
    } else {
      PollResponse(poll.id, poll.name, poll.owner, poll.pollOptions, poll.startSnapshotOrdinal, poll.endSnapshotOrdinal, "Open")
    }
  }

  def getAllPolls: IO[Response[IO]] = {
    val calculatedState = getCalculatedState
    calculatedState.flatMap { state =>
      val pollsResponse = state._2.polls.map { case (_, value) => formatPoll(value, state._1.value.value) }
      Ok(pollsResponse)
    }

  }

  def getPollById(pollId: String): IO[Response[IO]] = {
    val calculatedState = getCalculatedState
    calculatedState.flatMap { state =>
      val poll = state._2.polls.get(pollId)
      poll match {
        case Some(value) => Ok(formatPoll(value, state._1.value.value))
        case None => NotFound()
      }
    }
  }

}
