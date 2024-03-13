package com.my.nft.shared_data

import cats.data.NonEmptySet
import cats.effect.Async
import cats.syntax.foldable.toFoldableOps
import cats.syntax.traverse.toTraverseOps
import org.tessellation.schema.address.Address
import org.tessellation.security.SecurityProvider
import org.tessellation.security.signature.signature.SignatureProof

import java.net.URL
import scala.util.Try

object Utils {
  def isValidURL(url: String): Boolean =
    Try(new URL(url).toURI).isSuccess

  def getAllAddressesFromProofs[F[_] : Async : SecurityProvider](
    proofs: NonEmptySet[SignatureProof]
  ): F[List[Address]] =
    proofs
      .map(_.id)
      .toList
      .traverse(_.toAddress[F])

  def getFirstAddressFromProofs[F[_] : Async : SecurityProvider](
    proofs: NonEmptySet[SignatureProof]
  ): F[Address] =
    proofs.head.id.toAddress[F]
}

