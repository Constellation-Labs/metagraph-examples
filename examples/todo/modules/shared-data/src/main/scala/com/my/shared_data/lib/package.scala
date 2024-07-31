package com.my.shared_data

import cats.effect.Sync
import cats.implicits.{toFlatMapOps, toFunctorOps, toTraverseOps}

import scala.collection.immutable.SortedSet
import scala.reflect.ClassTag

import org.tessellation.currency.dataApplication.DataUpdate
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationBlock
import org.tessellation.currency.schema.currency.{CurrencyIncrementalSnapshot, DataApplicationPart}
import org.tessellation.json.JsonSerializer
import org.tessellation.security.hex.Hex
import org.tessellation.security.signature.Signed

import com.my.shared_data.lib.CirceOps.implicits._
import com.my.shared_data.lib.OrderedOps.implicits._
import com.my.shared_data.schema.Updates.TodoUpdate

package object lib {

  object syntax {

    implicit class ListSignedUpdateOps(lst: List[Signed[TodoUpdate]]) {
      def toSortedSet: SortedSet[Signed[TodoUpdate]] = SortedSet(lst: _*)
    }

    implicit class CurrencyIncrementalSnapshotOps[F[_]: Sync: JsonSerializer](cis: CurrencyIncrementalSnapshot) {

      def countUpdates: F[Long] = getBlocks.map(_.map(_.updates.size.toLong).sum)

      def getUpdates[U <: DataUpdate: ClassTag]: F[List[Signed[U]]] =
        getBlocks
          .map {
            _.flatMap { block =>
              block.updates.toList.collect { case s: U =>
                Signed(s.value.asInstanceOf[U], s.proofs)
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
