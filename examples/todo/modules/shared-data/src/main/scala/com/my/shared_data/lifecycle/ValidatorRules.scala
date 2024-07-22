package com.my.shared_data.lifecycle

import org.tessellation.currency.dataApplication.DataApplicationValidationError

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

object ValidatorRules {

  object Errors {

    @derive(decoder, encoder)
    case class InvalidFieldSize(fieldName: String, maxSize: Long) extends DataApplicationValidationError {
      val message = s"Invalid field size: $fieldName, maxSize: $maxSize"
    }

    @derive(decoder, encoder)
    case class FailedToDecodeEventData(violations: String) extends DataApplicationValidationError {
      val message = s"Failed to decode data with: ${violations}"
    }

    @derive(decoder, encoder)
    case object RequiredNonzeroFieldContainsZero extends DataApplicationValidationError {
      val message = s"A required non-zero field(s) found to contain zero"
    }

    @derive(decoder, encoder)
    case object RecordAlreadyExists extends DataApplicationValidationError {
      val message = s"Failed to create event, previous record found."
    }

    @derive(decoder, encoder)
    case object RecordDoesNotExist extends DataApplicationValidationError {
      val message = s"Failed to create event, no previous record found."
    }

    @derive(decoder, encoder)
    case object UpdateFailsToAdvanceMessageCounter extends DataApplicationValidationError {
      val message = s"Failed to create event, message counter is not advanced."
    }
  }
}
