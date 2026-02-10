package com.my.water_and_energy_usage.shared_data

import cats.data.NonEmptySet
import cats.effect.Async
import cats.syntax.all._
import com.my.water_and_energy_usage.shared_data.serializers.Serializers
import com.my.water_and_energy_usage.shared_data.types.Types.UsageUpdate
import io.circe.Json
import io.constellationnetwork.schema.address.Address
import io.constellationnetwork.security.SecurityProvider
import io.constellationnetwork.security.hash.Hash
import io.constellationnetwork.security.signature.signature.SignatureProof

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
