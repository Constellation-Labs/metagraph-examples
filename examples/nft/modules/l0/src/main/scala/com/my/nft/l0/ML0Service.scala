package com.my.nft.l0

import cats.data.NonEmptyList
import cats.effect.Async
import cats.syntax.applicative._
import cats.syntax.functor._

import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{
  BaseDataApplicationL0Service,
  DataApplicationL0Service,
  DataState,
  L0NodeContext
}
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.hash.Hash
import org.tessellation.security.signature.Signed

import com.my.nft.shared_data.errors.Errors.valid
import com.my.nft.shared_data.schema.{NFTUpdate, NFTUpdatesCalculatedState, NFTUpdatesState}

import io.constellationnetwork.metagraph_sdk.MetagraphCommonService
import io.constellationnetwork.metagraph_sdk.lifecycle.{CheckpointService, CombinerService, ValidationService}
import io.constellationnetwork.metagraph_sdk.std.Checkpoint
import io.constellationnetwork.metagraph_sdk.std.JsonBinaryHasher.FromJsonBinaryCodec
import org.http4s.HttpRoutes

object ML0Service {

  def make[F[+_]: Async](
    checkpointService: CheckpointService[F, NFTUpdatesCalculatedState],
    combinerService:   CombinerService[F, NFTUpdate, NFTUpdatesState, NFTUpdatesCalculatedState],
    validationService: ValidationService[F, NFTUpdate, NFTUpdatesState, NFTUpdatesCalculatedState]
  ): BaseDataApplicationL0Service[F] =
    BaseDataApplicationL0Service[F, NFTUpdate, NFTUpdatesState, NFTUpdatesCalculatedState](
      new MetagraphCommonService[F, NFTUpdate, NFTUpdatesState, NFTUpdatesCalculatedState]
        with DataApplicationL0Service[F, NFTUpdate, NFTUpdatesState, NFTUpdatesCalculatedState] {

        override def genesis: DataState[NFTUpdatesState, NFTUpdatesCalculatedState] =
          DataState(NFTUpdatesState.genesis, NFTUpdatesCalculatedState.genesis)

        override def validateData(
          state:   DataState[NFTUpdatesState, NFTUpdatesCalculatedState],
          updates: NonEmptyList[Signed[NFTUpdate]]
        )(implicit context: L0NodeContext[F]): F[DataApplicationValidationErrorOr[Unit]] =
          validationService.validateData(state, updates)

        override def validateUpdate(
          update: NFTUpdate
        )(implicit context: L0NodeContext[F]): F[DataApplicationValidationErrorOr[Unit]] =
          valid.pure[F]

        override def combine(
          state:   DataState[NFTUpdatesState, NFTUpdatesCalculatedState],
          updates: List[Signed[NFTUpdate]]
        )(implicit context: L0NodeContext[F]): F[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]] =
          combinerService.foldLeft(state, updates)

        override def routes(implicit context: L0NodeContext[F]): HttpRoutes[F] =
          CustomRoutes[F](checkpointService).public

        override def getCalculatedState(implicit
          context: L0NodeContext[F]
        ): F[(SnapshotOrdinal, NFTUpdatesCalculatedState)] =
          checkpointService.get.map { case Checkpoint(ordinal, state) => ordinal -> state }

        override def setCalculatedState(
          ordinal: SnapshotOrdinal,
          state:   NFTUpdatesCalculatedState
        )(implicit context: L0NodeContext[F]): F[Boolean] =
          checkpointService.set(Checkpoint(ordinal, state))

        override def hashCalculatedState(
          state: NFTUpdatesCalculatedState
        )(implicit context: L0NodeContext[F]): F[Hash] =
          state.hash
      }
    )

}
