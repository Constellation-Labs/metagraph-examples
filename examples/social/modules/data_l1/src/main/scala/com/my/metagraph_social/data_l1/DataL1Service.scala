package com.my.metagraph_social.data_l1

import cats.effect.Async
import com.my.metagraph_social.shared_data.LifecycleSharedFunctions
import com.my.metagraph_social.shared_data.types.States._
import com.my.metagraph_social.shared_data.types.Updates._
import com.my.metagraph_social.shared_data.types.codecs.DataUpdateCodec._
import io.circe.{Decoder, Encoder}
import io.constellationnetwork.currency.dataApplication._
import io.constellationnetwork.currency.dataApplication.dataApplication.{DataApplicationBlock, DataApplicationValidationErrorOr}
import io.constellationnetwork.json.JsonSerializer
import io.constellationnetwork.security.signature.Signed
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.{EntityDecoder, HttpRoutes}

object DataL1Service {

  def make[F[+_] : Async : JsonSerializer]: F[BaseDataApplicationL1Service[F]] = Async[F].delay {
    makeBaseDataApplicationL1Service
  }

  private def makeBaseDataApplicationL1Service[F[+_] : Async : JsonSerializer]: BaseDataApplicationL1Service[F] = BaseDataApplicationL1Service(
    new DataApplicationL1Service[F, SocialUpdate, SocialOnChainState, SocialCalculatedState] {

      override def validateUpdate(
        update: SocialUpdate
      )(implicit context: L1NodeContext[F]): F[DataApplicationValidationErrorOr[Unit]] =
        Async[F].pure(LifecycleSharedFunctions.validateUpdate(update))

      override def routes(implicit context: L1NodeContext[F]): HttpRoutes[F] =
        HttpRoutes.empty

      override def dataEncoder: Encoder[SocialUpdate] =
        implicitly[Encoder[SocialUpdate]]

      override def dataDecoder: Decoder[SocialUpdate] =
        implicitly[Decoder[SocialUpdate]]

      override def calculatedStateEncoder: Encoder[SocialCalculatedState] =
        implicitly[Encoder[SocialCalculatedState]]

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

      override def serializeCalculatedState(
        state: SocialCalculatedState
      ): F[Array[Byte]] =
        JsonSerializer[F].serialize[SocialCalculatedState](state)

      override def deserializeCalculatedState(
        bytes: Array[Byte]
      ): F[Either[Throwable, SocialCalculatedState]] =
        JsonSerializer[F].deserialize[SocialCalculatedState](bytes)
    }
  )
}
