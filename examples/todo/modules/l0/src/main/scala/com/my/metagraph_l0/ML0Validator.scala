package com.my.metagraph_l0

import cats.Applicative

import org.tessellation.currency.dataApplication.DataState
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.security.signature.Signed

import com.my.shared_data.lib.UpdateValidator
import com.my.shared_data.schema.Updates.TodoUpdate
import com.my.shared_data.schema.{CalculatedState, OnChain, Updates}

trait ML0Validator[F[_], U, T] extends UpdateValidator[F, U, T]

object ML0Validator {

  type STX = Signed[TodoUpdate]
  type DS = DataState[OnChain, CalculatedState]

  def make[F[_]: Applicative]: ML0Validator[F, STX, DS] =
    new ML0Validator[F, STX, DS] {

      override def verify(state: DS, signedUpdate: STX): F[DataApplicationValidationErrorOr[Unit]] =
        signedUpdate.value match {
          case Updates.CreateTask(dueData) => ???
          case Updates.ModifyTask(id)      => ???
          case Updates.CompleteTask(id)    => ???
          case Updates.RemoveTask(id)      => ???
        }

//      private def nominateEvent(
//        update: Signed[Updates.Nominate]
//      ): DS => F[DataApplicationValidationErrorOr[Unit]] = (state: DS) => {
//
//
//        List(
//          ValidatorRules.isValidEventData[MissionTask](update.data),
//          ValidatorRules.taskIdDoesNotExist(update.value, state.calculated)
//        ).combineAll
//      }
//
//      private def collectEvent(
//        update: Signed[Updates.Collect]
//      ): DS => DataApplicationValidationErrorOr[Unit] = (state: DS) =>
//        List(
//          ValidatorRules.isValidEventData[MissionTask](update.data),
//          ValidatorRules.taskIdDoesExist(update.value, state.calculated)
//        ).combineAll
//
//      private def closeEvent(
//        update: Signed[Updates.Close]
//      ): DS => DataApplicationValidationErrorOr[Unit] = (state: DS) =>
//        List(
//          ValidatorRules.isValidEventData[MissionTask](update.data),
//          ValidatorRules.taskIdDoesExist(update.value, state.calculated)
//        ).combineAll
//
//      private def abortEvent(
//        update: Signed[Updates.Abort]
//      ): DS => DataApplicationValidationErrorOr[Unit] = (state: DS) =>
//        List(
//          ValidatorRules.isValidEventData[MissionTask](update.data),
//          ValidatorRules.taskIdDoesExist(update.value, state.calculated)
//        ).combineAll
//
//      private def voteEvent(
//        update: Signed[Updates.Vote]
//      ): DS => DataApplicationValidationErrorOr[Unit] = (state: DS) =>
//        List(
//          ValidatorRules.isValidEventData[MultiUserTask](update.data),
//          ValidatorRules.taskIdDoesExist(update.value, state.calculated)
//        ).combineAll
//
//      private def pushEvent(
//        update: Signed[Updates.Push]
//      ): DS => DataApplicationValidationErrorOr[Unit] = (state: DS) =>
//        List(
//          ValidatorRules.isValidEventData[AircraftEvent](update.data),
//          ValidatorRules.updateIncrementsCounter(update, state.onChain)
//        ).combineAll
    }
}
