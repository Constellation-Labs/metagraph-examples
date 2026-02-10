package com.my.shared_data

import cats.effect.Sync
import cats.implicits.{toFlatMapOps, toFunctorOps, toTraverseOps}

import scala.collection.immutable.SortedSet
import scala.reflect.ClassTag

import io.constellationnetwork.currency.dataApplication.DataUpdate
import io.constellationnetwork.currency.dataApplication.dataApplication.DataApplicationBlock
import io.constellationnetwork.currency.schema.currency.{CurrencyIncrementalSnapshot, DataApplicationPart}
import io.constellationnetwork.json.JsonSerializer
import io.constellationnetwork.security.signature.Signed

import com.my.shared_data.lib.CirceOps.implicits._
import com.my.shared_data.lib.OrderedOps.implicits._
import com.my.shared_data.schema.Updates.TodoUpdate

package object lib {

  object syntax {

    implicit class ListSignedUpdateOps(lst: List[Signed[TodoUpdate]]) {
      def toSortedSet: SortedSet[Signed[TodoUpdate]] = SortedSet(lst: _*)
    }

    implicit class CurrencyIncrementalSnapshotOps[F[_]: Sync: JsonSerializer](cis: CurrencyIncrementalSnapshot) {

      def countUpdates: F[Long] = getBlocks.map(_.map(_.value.dataTransactions.toList.flatMap(_.toList).size.toLong).sum)

      def getUpdates[U <: DataUpdate: ClassTag]: F[List[Signed[U]]] =
        getBlocks
          .map {
            _.flatMap { block =>
              block.value.dataTransactions.toList.flatMap(_.toList).collect {
                case s @ Signed(_: U, _) => s.asInstanceOf[Signed[U]]
              }
            }.distinct
          }

      def getBlocks: F[List[Signed[DataApplicationBlock]]] =
        getPart.flatMap {
          _.blocks.traverse { bytes =>
            JsonBinaryCodec[F].deserialize[Signed[DataApplicationBlock]](bytes).flatMap(Sync[F].fromEither)
          }
        }

      def getPart: F[DataApplicationPart] =
        Sync[F].fromOption(cis.dataApplication, new RuntimeException(s"Failed to access Data Application Part"))
    }
  }
}
