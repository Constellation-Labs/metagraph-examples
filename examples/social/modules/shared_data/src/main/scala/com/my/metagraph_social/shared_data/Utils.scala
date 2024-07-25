package com.my.metagraph_social.shared_data

import cats.Functor
import cats.data.NonEmptySet
import cats.effect.Async
import cats.syntax.functor._
import org.tessellation.currency.dataApplication.{L0NodeContext, L1NodeContext}
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.address.Address
import org.tessellation.security.SecurityProvider
import org.tessellation.security.signature.signature.SignatureProof


object Utils {
  def getFirstAddressFromProofs[F[_] : Async : SecurityProvider](
    proofs: NonEmptySet[SignatureProof]
  ): F[Address] = {
    proofs.map(_.id).head.toAddress[F]
  }

  def getLastCurrencySnapshotOrdinal[F[_] : Functor](context: Either[L0NodeContext[F], L1NodeContext[F]]): F[Option[SnapshotOrdinal]] = {
    context match {
      case Left(l0Context) =>
        l0Context.getLastCurrencySnapshot.map(_.map(_.ordinal))
      case Right(l1Context) =>
        l1Context.getLastCurrencySnapshot.map(_.map(_.ordinal))
    }
  }
}

