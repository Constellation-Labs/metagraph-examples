package com.my.currency.data_l1

import cats.data.OptionT
import cats.effect.IO
import cats.implicits.catsSyntaxOption
import com.my.currency.shared_data.MainData.{Poll, deserializeState}
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.tessellation.currency.dataApplication.L1NodeContext
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

  def getAllPolls(implicit context: L1NodeContext[IO]): IO[Response[IO]] = {
    val lastSnapshotOrdinal = OptionT(context.getLastCurrencySnapshot).map(_.ordinal).value
    lastSnapshotOrdinal.flatMap {
      case Some(ordinal) =>
        OptionT(context.getLastCurrencySnapshot)
          .flatMap(_.data.toOptionT)
          .flatMapF(deserializeState(_).map(_.toOption))
          .value
          .flatMap {
            case Some(value) =>
              val pollsResponse = value.polls.map { case (_, value) => formatPoll(value, ordinal.value.value) }
              Ok(pollsResponse)
            case None =>
              NotFound()
          }
      case None => NotFound()
    }
  }

  def getPollById(pollId: String)(implicit context: L1NodeContext[IO]): IO[Response[IO]] = {
    val lastSnapshotOrdinal = OptionT(context.getLastCurrencySnapshot).map(_.ordinal).value
    lastSnapshotOrdinal.flatMap {
      case Some(ordinal) =>
        OptionT(context.getLastCurrencySnapshot)
          .flatMap(_.data.toOptionT)
          .flatMapF(deserializeState(_).map(_.toOption))
          .value
          .flatMap {
            case Some(value) =>
              val poll = value.polls.get(pollId)
              poll match {
                case Some(value) => Ok(formatPoll(value, ordinal.value.value))
                case None => NotFound()
              }
            case None =>
              NotFound()
          }
      case None => NotFound()
    }
  }

}
