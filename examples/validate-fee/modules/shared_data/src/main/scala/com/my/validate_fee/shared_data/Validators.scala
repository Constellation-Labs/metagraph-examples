package com.my.validate_fee.shared_data

import cats.syntax.all._
import com.my.validate_fee.shared_data.FeeCalculators._
import com.my.validate_fee.shared_data.errors.Errors.{DataApplicationValidationTypeOps, unexpectedAmount, unexpectedParentHash, unexpectedParentOrdinal, valid}
import com.my.validate_fee.shared_data.types.Types.UpdateWithFee
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.schema.feeTransaction.FeeTransaction
import org.tessellation.schema.transaction.TransactionOrdinal
import org.tessellation.security.hash.Hash

object Validators {
  def validateFeeTransaction(
    update: UpdateWithFee,
    maybeStoredFeeTransactionAndHash: Option[(FeeTransaction, Hash)]
  ): DataApplicationValidationErrorOr[Unit] = {
    def checkAmount = {
      val expected = calculateFee(update)
      unexpectedAmount(expected, update).whenA(update.fee.amount < expected)
    }

    def checkOrdinal(ordinal: TransactionOrdinal) = unexpectedParentOrdinal(ordinal, update.fee.parent.ordinal)
      .unlessA(ordinal <= update.fee.parent.ordinal)

    def checkHash(hash: Hash) = unexpectedParentHash(hash, update.fee.parent.hash)
      .unlessA(hash === update.fee.parent.hash)

    maybeStoredFeeTransactionAndHash match {
      case None => checkAmount
      case Some((feeTxn, _)) if feeTxn == update.fee => valid
      case Some((feeTxn, hash)) => checkAmount |+| checkOrdinal(feeTxn.ordinal) |+| checkHash(hash)
    }
  }
}
