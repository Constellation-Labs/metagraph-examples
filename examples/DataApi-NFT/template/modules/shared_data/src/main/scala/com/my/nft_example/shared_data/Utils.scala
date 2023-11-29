package com.my.nft_example.shared_data

import cats.data.NonEmptySet
import cats.effect.Async
import cats.implicits.{toFoldableOps, toTraverseOps}
import org.tessellation.schema.address.Address
import org.tessellation.security.SecurityProvider
import org.tessellation.security.signature.signature.SignatureProof

import java.net.{MalformedURLException, URISyntaxException, URL}
import scala.util.{Failure, Success, Try}

object Utils {

  @throws[MalformedURLException]
  @throws[URISyntaxException]
  def isValidURL(url: String): Boolean =
    Try(new URL(url).toURI) match {
      case Failure(_) => false
      case Success(_) => true
    }

  def getAllAddressesFromProofs[F[_] : Async : SecurityProvider](
    proofs: NonEmptySet[SignatureProof]
  ): F[List[Address]] = {
    proofs
      .map(_.id)
      .toList
      .traverse(_.toAddress[F])
  }

  def getFirstAddressFromProofs[F[_] : Async : SecurityProvider](
    proofs: NonEmptySet[SignatureProof]
  ): F[Address] = {
    proofs
      .map(_.id)
      .toList
      .head
      .toAddress[F]
  }
}

