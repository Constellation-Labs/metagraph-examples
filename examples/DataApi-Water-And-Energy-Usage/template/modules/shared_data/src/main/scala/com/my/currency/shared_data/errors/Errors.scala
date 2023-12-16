package com.my.currency.shared_data.errors

import cats.syntax.all._
import org.tessellation.currency.dataApplication.DataApplicationValidationError
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr

object Errors {
  type DataApplicationValidationType = DataApplicationValidationErrorOr[Unit]

  val valid: DataApplicationValidationType =
    ().validNec[DataApplicationValidationError]

  implicit class DataApplicationValidationTypeOps[E <: DataApplicationValidationError](err: E) {
    def invalid: DataApplicationValidationType =
      err.invalidNec[Unit]

    def unless(
      cond: Boolean
    ): DataApplicationValidationType =
      if (cond) valid else invalid

    def when(
      cond: Boolean
    ): DataApplicationValidationType =
      if (cond) invalid else valid
  }
  case object EnergyNotPositive extends DataApplicationValidationError {
    val message = "Energy usage must be positive"
  }

  case object EnergyUpdateOutdated extends DataApplicationValidationError {
    val message = "Energy update older than latest update timestamp, rejecting"
  }

  case object WaterNotPositive extends DataApplicationValidationError {
    val message = "Water usage must be positive"
  }

  case object WaterUpdateOutdated extends DataApplicationValidationError {
    val message = "Water update older than latest update timestamp, rejecting"
  }

  case object EmptyUpdate extends DataApplicationValidationError {
    val message = "Provided an empty update"
  }

  case object InvalidAddress extends DataApplicationValidationError {
    val message = "Provided address different than proof"
  }
}