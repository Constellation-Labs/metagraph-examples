package com.my.custom_validator.l1

import java.util.UUID

import eu.timepit.refined.auto._
import io.constellationnetwork.BuildInfo
import io.constellationnetwork.currency.l1.CurrencyL1App
import io.constellationnetwork.dag.l1.domain.transaction.ContextualTransactionValidator.CustomValidationError
import io.constellationnetwork.dag.l1.domain.transaction.{CustomContextualTransactionValidator, TransactionValidatorContext}
import io.constellationnetwork.schema.cluster.ClusterId
import io.constellationnetwork.schema.semver.{MetagraphVersion, TessellationVersion}
import io.constellationnetwork.schema.transaction.Transaction
import io.constellationnetwork.security.Hashed

object Main
  extends CurrencyL1App(
    "custom-transaction-validation-l1",
    "custom-transaction-validation L1 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version),
    metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version)
  ) {
  override def transactionValidator: Option[CustomContextualTransactionValidator] = Some {
    (hashedTransaction: Hashed[Transaction], context: TransactionValidatorContext) =>
      Either.cond(
        context.currentOrdinal.value < 10L || hashedTransaction.fee.value > 10L,
        hashedTransaction,
        CustomValidationError("Fee must be greater than 10")
      )
  }
}
