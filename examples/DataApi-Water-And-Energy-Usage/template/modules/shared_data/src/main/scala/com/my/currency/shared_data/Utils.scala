package com.my.currency.shared_data

import cats.data.NonEmptySet
import cats.effect.Async
import cats.syntax.all._
import com.my.currency.shared_data.serializers.Serializers
import com.my.currency.shared_data.types.Types.UsageUpdate
import io.circe.Json
import org.tessellation.schema.address.Address
import org.tessellation.security.SecurityProvider
import org.tessellation.security.hash.Hash
import org.tessellation.security.signature.signature.SignatureProof

object Utils {

  def getUsageUpdateHash(
    update: UsageUpdate
  ): String =
    Hash.fromBytes(Serializers.serializeUpdate(update)).value

  def getAllAddressesFromProofs[F[_] : Async : SecurityProvider](
    proofs: NonEmptySet[SignatureProof]
  ): F[List[Address]] =
    proofs
      .map(_.id)
      .toList
      .traverse(_.toAddress[F])

   def removeKeyFromJSON(json: Json, keyToRemove: String): Json =
    json.mapObject { obj =>
      obj.remove(keyToRemove).mapValues {
        case objValue: Json => removeKeyFromJSON(objValue, keyToRemove)
        case other => other
      }
    }.mapArray { arr =>
      arr.map(removeKeyFromJSON(_, keyToRemove))
    }
}
