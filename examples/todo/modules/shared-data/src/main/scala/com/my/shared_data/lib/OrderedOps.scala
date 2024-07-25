package com.my.shared_data.lib

import com.my.shared_data.schema.Updates._

object OrderedOps {

  object implicits {

    implicit val todoUpdateOrdering: Ordering[TodoUpdate] =
      new Ordering[TodoUpdate] {

        def compare(x: TodoUpdate, y: TodoUpdate): Int = ???
      }
  }
}
