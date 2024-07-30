package com.my.validate_fee.shared_data

import com.my.validate_fee.shared_data.types.Types.{UpdateTypeOne, UpdateTypeTwo, UpdateWithFee}
import eu.timepit.refined.auto._
import org.tessellation.schema.balance.Amount

object FeeCalculators {

  def calculateFee(update: UpdateWithFee): Amount =
    update match {
      case UpdateTypeOne(_, _) => Amount(123L)
      case UpdateTypeTwo(_, _) => Amount(234L)
    }
}
