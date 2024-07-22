package com.my.data_l1

import cats.effect.Async

import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr

import com.my.shared_data.lib.UpdateValidator
import com.my.shared_data.schema.Updates.TodoUpdate
import com.my.shared_data.schema.{OnChain, Updates}

trait DataL1Validator[F[_], U, T] extends UpdateValidator[F, U, T]

object DataL1Validator {

  def make[F[_]: Async]: DataL1Validator[F, TodoUpdate, OnChain] =
    new DataL1Validator[F, TodoUpdate, OnChain] {

      override def verify(state: OnChain, update: TodoUpdate): F[DataApplicationValidationErrorOr[Unit]] =
        update match {
          case Updates.CreateTask(dueData) => ???
          case Updates.ModifyTask(id)      => ???
          case Updates.CompleteTask(id)    => ???
          case Updates.RemoveTask(id)      => ???
        }

//      private def nominateEvent(
//        update: Updates.Nominate
//      ): OnChain => DataApplicationValidationErrorOr[Unit] = (state: OnChain) =>
//        List(
//          ValidatorRules.isValidEventData[MissionTask](update.data)
//        ).combineAll
//
//      private def collectEvent(
//        update: Updates.Collect
//      ): OnChain => DataApplicationValidationErrorOr[Unit] = (state: OnChain) =>
//        List(
//          ValidatorRules.isValidEventData[MissionTask](update.data)
//        ).combineAll
//
//      private def closeEvent(
//        update: Updates.Close
//      ): OnChain => DataApplicationValidationErrorOr[Unit] = (state: OnChain) =>
//        List(
//          ValidatorRules.isValidEventData[MissionTask](update.data)
//        ).combineAll
//
//      private def abortEvent(
//        update: Updates.Abort
//      ): OnChain => DataApplicationValidationErrorOr[Unit] = (state: OnChain) =>
//        List(
//          ValidatorRules.isValidEventData[MissionTask](update.data)
//        ).combineAll
//
//      private def voteEvent(
//        update: Updates.Vote
//      ): OnChain => DataApplicationValidationErrorOr[Unit] = (state: OnChain) =>
//        List(
//          ValidatorRules.isValidEventData[MultiUserTask](update.data)
//        ).combineAll
//
//      private def pushEvent(
//        update: Updates.Push
//      ): OnChain => DataApplicationValidationErrorOr[Unit] = (state: OnChain) =>
//        List(
//          ValidatorRules.isValidEventData[AircraftEvent](update.data),
//          ValidatorRules.updateIncrementsCounter(update, state)
//        ).combineAll
    }
}
