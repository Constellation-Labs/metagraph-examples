package com.my.data_l1

import cats.effect.{Async, Clock}
import cats.implicits.{toFlatMapOps, toFunctorOps}
import cats.syntax.either._

import org.tessellation.currency.dataApplication.{DataApplicationValidationError, L1NodeContext}
import org.tessellation.json.JsonSerializer

import com.my.data_l1.DataL1NodeContext.syntax._
import com.my.shared_data.lib.MetagraphPublicRoutes

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.syntax.EncoderOps
import org.http4s.HttpRoutes

class DataL1CustomRoutes[F[_]: Async: JsonSerializer](implicit context: L1NodeContext[F])
    extends MetagraphPublicRoutes[F] {

  protected val routes: HttpRoutes[F] = HttpRoutes.empty
}

object DataL1CustomRoutes {

  object Errors {

    @derive(decoder, encoder)
    case class EventIdNotFound(eventId: String) extends DataApplicationValidationError {
      val message: String = s"Event ID $eventId was not found"
    }

    @derive(decoder, encoder)
    case class EventRecordNotFound(eventId: String, nonce: Long) extends DataApplicationValidationError {
      val message: String = s"Event Record with ID $eventId and nonce $nonce was not found"
    }
  }
}
