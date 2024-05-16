package com.my.custom_validator.l1

import java.util.UUID
import org.tessellation.BuildInfo
import org.tessellation.currency.l1.CurrencyL1App
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.schema.transaction.Transaction
import org.tessellation.schema.semver.MetagraphVersion
import org.tessellation.schema.semver.TessellationVersion
import org.tessellation.security.Hashed
import org.tessellation.dag.l1.domain.transaction.CustomContextualTransactionValidator
import org.tessellation.dag.l1.domain.transaction.ContextualTransactionValidator.CustomValidationError
import org.tessellation.dag.l1.domain.transaction.TransactionValidatorContext
import eu.timepit.refined.auto._

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
