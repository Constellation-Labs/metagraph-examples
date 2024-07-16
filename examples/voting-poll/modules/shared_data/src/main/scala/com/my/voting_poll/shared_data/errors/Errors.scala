package com.my.voting_poll.shared_data.errors

import cats.implicits.catsSyntaxValidatedIdBinCompat0
import org.tessellation.currency.dataApplication.DataApplicationValidationError
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr

object Errors {
  type DataApplicationValidationType = DataApplicationValidationErrorOr[Unit]

  val valid: DataApplicationValidationType = ().validNec[DataApplicationValidationError]

  implicit class DataApplicationValidationTypeOps[E <: DataApplicationValidationError](err: E) {
    def invalid: DataApplicationValidationType = err.invalidNec[Unit]

    def unlessA(cond: Boolean): DataApplicationValidationType = if (cond) valid else invalid

    def whenA(cond: Boolean): DataApplicationValidationType = if (cond) invalid else valid
  }

  case object PollAlreadyExists extends DataApplicationValidationError {
    val message = "Poll already exists"
  }

  case object PollDoesNotExists extends DataApplicationValidationError {
    val message = "Poll does not exists"
  }

  case object InvalidOption extends DataApplicationValidationError {
    val message = "Invalid option"
  }

  case object InvalidAddress extends DataApplicationValidationError {
    val message = "Provided address different than proof"
  }

  case object RepeatedVote extends DataApplicationValidationError {
    val message = "This user already voted!"
  }

  case object InvalidEndSnapshot extends DataApplicationValidationError {
    val message = "Provided end snapshot ordinal lower than current snapshot!"
  }

  case object CouldNotGetLatestCurrencySnapshot extends DataApplicationValidationError {
    val message = "Could not get latest currency snapshot!"
  }

  case object NotStartedPool extends DataApplicationValidationError {
    val message = "Not started pool"
  }

  case object ClosedPool extends DataApplicationValidationError {
    val message = "Pool is closed"
  }

  case object NotEnoughWalletBalance extends DataApplicationValidationError {
    val message = "Not enough wallet balance"
  }
}

