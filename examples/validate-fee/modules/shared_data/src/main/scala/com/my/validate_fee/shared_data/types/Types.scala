package com.my.validate_fee.shared_data.types

import cats.{Monoid, Semigroup}
import cats.syntax.all._
import derevo.cats.show
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.tessellation.currency.dataApplication.{DataCalculatedState, DataOnChainState, DataUpdate}
import org.tessellation.currency.schema.feeTransaction.FeeTransaction

object Types {
  @derive(decoder, encoder, show)
  sealed trait UpdateWithFee extends DataUpdate {
    def name: String
    def address: String
    def fee: FeeTransaction
  }

  object UpdateWithFee {
    implicit val semigroup: Semigroup[UpdateWithFee] =
      Semigroup.instance((a, b) => if (a.fee.parent.ordinal < b.fee.parent.ordinal) b else a)
  }

  @derive(decoder, encoder, show)
  case class UpdateTypeOne(name: String, fee: FeeTransaction) extends UpdateWithFee {
    override def address: String = fee.destination.value.value
  }

  @derive(decoder, encoder, show)
  case class UpdateTypeTwo(name: String, fee: FeeTransaction) extends UpdateWithFee {
    override def address: String = fee.destination.value.value
  }

  @derive(decoder, encoder)
  case class UpdateWithFeeChainState(updates: List[UpdateWithFee]) extends DataOnChainState

  @derive(decoder, encoder, show)
  case class CalculatedUpdateWithFeeState(stateMap: Map[String, UpdateWithFee]) extends DataCalculatedState
  object CalculatedUpdateWithFeeState {
    val empty: CalculatedUpdateWithFeeState = CalculatedUpdateWithFeeState(Map.empty)

    implicit val monoid: Monoid[CalculatedUpdateWithFeeState] =
      Monoid.instance[CalculatedUpdateWithFeeState](
        empty,
        (a, b) => CalculatedUpdateWithFeeState(
          a.stateMap.foldLeft(b.stateMap) {
            case (acc, (address, a)) =>
              acc.updatedWith(address) {
                case None => a.some
                case Some(a0) => (a0 |+| a).some
              }
          }
        )
      )
  }
}
