package com.my.validate_fee.shared_data.errors

import cats.syntax.all._
import com.my.validate_fee.shared_data.types.Types.UpdateWithFee
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataApplicationFeeError, DataApplicationValidationError}
import org.tessellation.schema.balance.Amount
import org.tessellation.schema.transaction.TransactionOrdinal
import org.tessellation.security.hash.Hash

object Errors {
  type DataApplicationValidationType = DataApplicationValidationErrorOr[Unit]

  val valid: DataApplicationValidationType = ().validNec[DataApplicationValidationError]

  implicit class DataApplicationValidationTypeOps[E <: DataApplicationValidationError](err: E) {
    def invalid: DataApplicationValidationType = err.invalidNec[Unit]

    def unlessA(cond: Boolean): DataApplicationValidationType = if (cond) valid else invalid

    def whenA(cond: Boolean): DataApplicationValidationType = if (cond) invalid else valid
  }

  def unexpectedAmount(expected: Amount, update: UpdateWithFee): DataApplicationFeeError =
    DataApplicationFeeError(s"Expected fee amount of ${expected.show}: ${update.show}")

  def unexpectedParentOrdinal(expected: TransactionOrdinal, actual: TransactionOrdinal): DataApplicationFeeError =
    DataApplicationFeeError(s"Given fee transaction ordinal in ${actual.show} must be greater than or equal to ${expected.show}")

  def unexpectedParentHash(expected: Hash, actual: Hash): DataApplicationFeeError =
    DataApplicationFeeError(s"Expected parent hash of ${expected.show} but update contains ${actual.show}")
}
