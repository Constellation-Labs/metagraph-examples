package com.my.shared_data.lifecycle

import cats.data.Validated
import cats.effect.{Async, Clock}
import cats.implicits.toFunctorOps
import cats.syntax.validated._

import scala.concurrent.duration.DurationInt

import org.tessellation.currency.dataApplication.DataApplicationValidationError
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

object ValidatorRules {


  object Errors {}
}
