package com.my.currency.shared_data

import cats.data.NonEmptySet
import cats.effect.Async
import cats.syntax.foldable.toFoldableOps
import cats.syntax.traverse.toTraverseOps
import com.my.currency.shared_data.serializers.Serializers
import com.my.currency.shared_data.types.Types.UsageUpdate
import org.tessellation.schema.address.Address
import org.tessellation.security.SecurityProvider
import org.tessellation.security.hash.Hash
import org.tessellation.security.signature.signature.SignatureProof

object Utils {

  def getUsageUpdateHash(
    update: UsageUpdate
  ): String =
    Hash.fromBytes(Serializers.serializeUpdate(update)).toString

  def getAllAddressesFromProofs[F[_] : Async : SecurityProvider](
    proofs: NonEmptySet[SignatureProof]
  ): F[List[Address]] =
    proofs
      .map(_.id)
      .toList
      .traverse(_.toAddress[F])
}
