package com.my.shared_data.schema

import org.tessellation.currency.dataApplication.DataUpdate

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

object Updates {

  @derive(decoder, encoder)
  sealed abstract class TodoUpdate extends DataUpdate

  @derive(decoder, encoder)
  final case class CreateTask(
    dueDate:     String,
    description: String,
    optStatus:   Option[TaskStatus]
  ) extends TodoUpdate

  @derive(decoder, encoder)
  final case class ModifyTask(
    id:         String,
    optStatus:  Option[TaskStatus],
    optDueDate: Option[String]
  ) extends TodoUpdate

  @derive(decoder, encoder)
  final case class CompleteTask(
    id: String
  ) extends TodoUpdate

  @derive(decoder, encoder)
  final case class RemoveTask(
    id: String
  ) extends TodoUpdate
}
