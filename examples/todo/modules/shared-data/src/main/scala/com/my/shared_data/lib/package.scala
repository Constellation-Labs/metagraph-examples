package com.my.shared_data

import scala.collection.immutable.SortedSet

import com.my.shared_data.lib.OrderedOps.implicits._
import com.my.shared_data.schema.Updates.TodoUpdate

import io.constellationnetwork.security.signature.Signed

package object lib {

  object syntax {

    implicit class ListSignedUpdateOps(lst: List[Signed[TodoUpdate]]) {
      def toSortedSet: SortedSet[Signed[TodoUpdate]] = SortedSet(lst: _*)
    }
  }
}
