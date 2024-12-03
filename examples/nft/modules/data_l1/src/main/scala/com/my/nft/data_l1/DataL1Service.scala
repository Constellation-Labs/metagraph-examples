package com.my.nft.data_l1

import cats.data.NonEmptyList
import cats.effect.Async
import cats.syntax.applicative._

import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{
  BaseDataApplicationL1Service,
  DataApplicationL1Service,
  DataState,
  L1NodeContext
}
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.hash.Hash
import org.tessellation.security.signature.Signed

import com.my.nft.shared_data.errors.Errors.valid
import com.my.nft.shared_data.schema.{NFTUpdate, NFTUpdatesCalculatedState, NFTUpdatesState}

import io.constellationnetwork.metagraph_sdk.MetagraphCommonService
import io.constellationnetwork.metagraph_sdk.lifecycle.ValidationService
import org.http4s.HttpRoutes

object DataL1Service {

  def make[F[+_]: Async](
    validationService: ValidationService[F, NFTUpdate, NFTUpdatesState, NFTUpdatesCalculatedState]
  ): BaseDataApplicationL1Service[F] =
    BaseDataApplicationL1Service[F, NFTUpdate, NFTUpdatesState, NFTUpdatesCalculatedState](
      new MetagraphCommonService[F, NFTUpdate, NFTUpdatesState, NFTUpdatesCalculatedState]
        with DataApplicationL1Service[F, NFTUpdate, NFTUpdatesState, NFTUpdatesCalculatedState] {

        override def validateData(
          state:   DataState[NFTUpdatesState, NFTUpdatesCalculatedState],
          updates: NonEmptyList[Signed[NFTUpdate]]
        )(implicit context: L1NodeContext[F]): F[DataApplicationValidationErrorOr[Unit]] =
          valid.pure[F]

        override def validateUpdate(
          update: NFTUpdate
        )(implicit context: L1NodeContext[F]): F[DataApplicationValidationErrorOr[Unit]] =
          validationService.validateUpdate(update)

        override def combine(
          state:   DataState[NFTUpdatesState, NFTUpdatesCalculatedState],
          updates: List[Signed[NFTUpdate]]
        )(implicit context: L1NodeContext[F]): F[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]] =
          state.pure[F]

        override def routes(implicit context: L1NodeContext[F]): HttpRoutes[F] =
          HttpRoutes.empty

        override def getCalculatedState(implicit
          context: L1NodeContext[F]
        ): F[(SnapshotOrdinal, NFTUpdatesCalculatedState)] =
          (SnapshotOrdinal.MinValue, NFTUpdatesCalculatedState.genesis).pure[F]

        override def setCalculatedState(
          ordinal: SnapshotOrdinal,
          state:   NFTUpdatesCalculatedState
        )(implicit context: L1NodeContext[F]): F[Boolean] =
          true.pure[F]

        override def hashCalculatedState(
          state: NFTUpdatesCalculatedState
        )(implicit context: L1NodeContext[F]): F[Hash] =
          Hash.empty.pure[F]
      }
    )
}
