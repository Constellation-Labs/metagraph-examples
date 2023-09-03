package com.my.currency.shared_data

import org.tessellation.currency.dataApplication.DataApplicationValidationError

object Errors {
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

  case object CouldNotGetLatestState extends DataApplicationValidationError {
    val message = "Could not get latest state!"
  }

  case object CouldNotGetLatestCurrencySnapshot extends DataApplicationValidationError {
    val message = "Could not get latest currency snapshot!"
  }
}