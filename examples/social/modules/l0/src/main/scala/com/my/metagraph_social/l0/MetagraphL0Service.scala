package com.my.metagraph_social.l0

import cats.data.NonEmptyList
import cats.effect.Async
import cats.syntax.all._
import com.my.metagraph_social.l0.custom_routes.CustomRoutes
import com.my.metagraph_social.shared_data.LifecycleSharedFunctions
import com.my.metagraph_social.shared_data.calculated_state.CalculatedStateService
import com.my.metagraph_social.shared_data.types.States._
import com.my.metagraph_social.shared_data.types.Updates._
import com.my.metagraph_social.shared_data.types.codecs.DataUpdateCodec._
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.{EntityDecoder, HttpRoutes}
import org.tessellation.currency.dataApplication._
import org.tessellation.currency.dataApplication.dataApplication.{DataApplicationBlock, DataApplicationValidationErrorOr}
import org.tessellation.json.JsonSerializer
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.SecurityProvider
import org.tessellation.security.hash.Hash
import org.tessellation.security.signature.Signed

object MetagraphL0Service {

  def make[F[+_] : Async : JsonSerializer](
    calculatedStateService: CalculatedStateService[F]
  ): F[BaseDataApplicationL0Service[F]] = Async[F].delay {
    makeBaseDataApplicationL0Service(
      calculatedStateService
    )
  }

  private def makeBaseDataApplicationL0Service[F[+_] : Async : JsonSerializer](
    calculatedStateService: CalculatedStateService[F]
  ): BaseDataApplicationL0Service[F] =
    BaseDataApplicationL0Service(
      new DataApplicationL0Service[F, SocialUpdate, SocialOnChainState, SocialCalculatedState] {
        override def genesis: DataState[SocialOnChainState, SocialCalculatedState] = {
          DataState(
            SocialOnChainState(List.empty),
            SocialCalculatedState(Map.empty)
          )
        }

        override def validateUpdate(
          update: SocialUpdate
        )(implicit context: L0NodeContext[F]): F[DataApplicationValidationErrorOr[Unit]] =
          Async[F].pure(LifecycleSharedFunctions.validateUpdate(update))

        override def validateData(
          state  : DataState[SocialOnChainState, SocialCalculatedState],
          updates: NonEmptyList[Signed[SocialUpdate]]
        )(implicit context: L0NodeContext[F]): F[DataApplicationValidationErrorOr[Unit]] = {
          implicit val sp: SecurityProvider[F] = context.securityProvider
          LifecycleSharedFunctions.validateData(state, updates)
        }

        override def combine(
          state  : DataState[SocialOnChainState, SocialCalculatedState],
          updates: List[Signed[SocialUpdate]]
        )(implicit context: L0NodeContext[F]): F[DataState[SocialOnChainState, SocialCalculatedState]] =
          LifecycleSharedFunctions.combine[F](
            state,
            updates
          )

        override def dataEncoder: Encoder[SocialUpdate] =
          implicitly[Encoder[SocialUpdate]]

        override def calculatedStateEncoder: Encoder[SocialCalculatedState] =
          implicitly[Encoder[SocialCalculatedState]]

        override def dataDecoder: Decoder[SocialUpdate] =
          implicitly[Decoder[SocialUpdate]]

        override def calculatedStateDecoder: Decoder[SocialCalculatedState] =
          implicitly[Decoder[SocialCalculatedState]]

        override def signedDataEntityDecoder: EntityDecoder[F, Signed[SocialUpdate]] =
          circeEntityDecoder

        override def serializeBlock(
          block: Signed[DataApplicationBlock]
        ): F[Array[Byte]] =
          JsonSerializer[F].serialize[Signed[DataApplicationBlock]](block)

        override def deserializeBlock(
          bytes: Array[Byte]
        ): F[Either[Throwable, Signed[DataApplicationBlock]]] =
          JsonSerializer[F].deserialize[Signed[DataApplicationBlock]](bytes)

        override def serializeState(
          state: SocialOnChainState
        ): F[Array[Byte]] =
          JsonSerializer[F].serialize[SocialOnChainState](state)

        override def deserializeState(
          bytes: Array[Byte]
        ): F[Either[Throwable, SocialOnChainState]] =
          JsonSerializer[F].deserialize[SocialOnChainState](bytes)

        override def serializeUpdate(
          update: SocialUpdate
        ): F[Array[Byte]] =
          JsonSerializer[F].serialize[SocialUpdate](update)

        override def deserializeUpdate(
          bytes: Array[Byte]
        ): F[Either[Throwable, SocialUpdate]] =
          JsonSerializer[F].deserialize[SocialUpdate](bytes)

        override def getCalculatedState(implicit context: L0NodeContext[F]): F[(SnapshotOrdinal, SocialCalculatedState)] =
          calculatedStateService.get.map(calculatedState => (calculatedState.ordinal, calculatedState.state))

        override def setCalculatedState(
          ordinal: SnapshotOrdinal,
          state  : SocialCalculatedState
        )(implicit context: L0NodeContext[F]): F[Boolean] =
          calculatedStateService.set(ordinal, state)

        override def hashCalculatedState(
          state: SocialCalculatedState
        )(implicit context: L0NodeContext[F]): F[Hash] =
          calculatedStateService.hash(state)

        override def routes(implicit context: L0NodeContext[F]): HttpRoutes[F] =
          CustomRoutes[F](calculatedStateService).public

        override def serializeCalculatedState(
          state: SocialCalculatedState
        ): F[Array[Byte]] =
          JsonSerializer[F].serialize[SocialCalculatedState](state)

        override def deserializeCalculatedState(
          bytes: Array[Byte]
        ): F[Either[Throwable, SocialCalculatedState]] =
          JsonSerializer[F].deserialize[SocialCalculatedState](bytes)
      })
}
