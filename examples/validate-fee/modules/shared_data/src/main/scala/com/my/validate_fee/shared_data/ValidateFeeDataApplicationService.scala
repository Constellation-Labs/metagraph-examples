package com.my.validate_fee.shared_data

import cats.Applicative
import cats.data.NonEmptyList
import cats.effect.Async
import cats.syntax.all._
import com.my.validate_fee.shared_data.Validators._
import com.my.validate_fee.shared_data.calculated_state.DataCalculatedStateService
import com.my.validate_fee.shared_data.deserializers.Deserializers
import com.my.validate_fee.shared_data.errors.Errors.valid
import com.my.validate_fee.shared_data.serializers.Serializers
import com.my.validate_fee.shared_data.types.Types.{CalculatedUpdateWithFeeState, UpdateWithFee, UpdateWithFeeChainState}
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.{EntityDecoder, HttpRoutes}
import org.tessellation.currency.dataApplication._
import org.tessellation.currency.dataApplication.dataApplication.{DataApplicationBlock, DataApplicationValidationErrorOr}
import org.tessellation.currency.schema.EstimatedFee
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.hash.Hash
import org.tessellation.security.signature.Signed
import org.typelevel.log4cats.SelfAwareStructuredLogger

abstract class ValidateFeeDataApplicationService[F[_] : Async, Context]
  extends DataApplicationService[F, UpdateWithFee, UpdateWithFeeChainState, CalculatedUpdateWithFeeState]
    with DataApplicationContextualOps[F, UpdateWithFee, UpdateWithFeeChainState, CalculatedUpdateWithFeeState, Context] {

  def logger: SelfAwareStructuredLogger[F]

  def calculatedStateService: DataCalculatedStateService[F, CalculatedUpdateWithFeeState]

  private def jsonHash[A: Encoder](a: A): F[Hash] =
    a.pure[F]
      .map(Serializers.serialize[A])
      .map(Hash.fromBytes)

  override def estimateFee(gsOrdinal: SnapshotOrdinal)(update: UpdateWithFee)(implicit context: Context, A: Applicative[F]): F[EstimatedFee] =
    logger.debug(s"estimateFee: $gsOrdinal\n${update.asJson.spaces2}") >>
      super.estimateFee(gsOrdinal)(update)

  override def validateFee(gsOrdinal: SnapshotOrdinal)(update: Signed[UpdateWithFee])(implicit context: Context, A: Applicative[F]): F[DataApplicationValidationErrorOr[Unit]] =
    for {
      (stateOrdinal, state) <- calculatedStateService.getCalculatedState
      maybeStored <- state.stateMap.get(update.address).traverse(u => jsonHash(u.fee).map((u.fee, _)))

      result = validateFeeTransaction(update, maybeStored)

      logMsg =
        s"""validateFee: ${gsOrdinal.show}\n${update.value.asJson.spaces2}
           |calculated state: ${stateOrdinal.show}, ${state.show}
           |stored update ${maybeStored.show}"
           |** RESULT: $result""".stripMargin

      _ <- logger.debug(logMsg)
    } yield result

  override def validateUpdate(update: UpdateWithFee)(implicit context: Context): F[DataApplicationValidationErrorOr[Unit]] =
    valid.pure[F]

  override def validateData(state: DataState[UpdateWithFeeChainState, CalculatedUpdateWithFeeState], updates: NonEmptyList[Signed[UpdateWithFee]])(implicit context: Context): F[DataApplicationValidationErrorOr[Unit]] =
    valid.pure[F]

  override def combine(state: DataState[UpdateWithFeeChainState, CalculatedUpdateWithFeeState], updates: List[Signed[UpdateWithFee]])(implicit context: Context): F[DataState[UpdateWithFeeChainState, CalculatedUpdateWithFeeState]] =
    Async[F].delay {
      val onChainState = UpdateWithFeeChainState(state.onChain.updates ++ updates.map(_.value))
      val calculatedState = CalculatedUpdateWithFeeState(updates.map(s => s.value.address -> s.value).toMap)
      DataState(onChain = onChainState, calculated = state.calculated |+| calculatedState)
    }.flatTap(newState => logger.debug(
      s"""combine
         |Before:
         |${state.onChain.asJson.spaces2}
         |${state.calculated.asJson.spaces2}
         |${updates.asJson.spaces2}
         |After:
         |${newState.onChain.asJson.spaces2}
         |${newState.calculated.asJson.spaces2}""".stripMargin
    ))

  override def serializeUpdate(update: UpdateWithFee): F[Array[Byte]] =
    Async[F].delay(Serializers.serializeUpdate(update))

  override def serializeState(state: UpdateWithFeeChainState): F[Array[Byte]] =
    Async[F].delay(Serializers.serializeState(state))

  override def serializeBlock(block: Signed[DataApplicationBlock]): F[Array[Byte]] =
    Async[F].delay(Serializers.serializeBlock(block)(dataEncoder.asInstanceOf[Encoder[DataUpdate]]))

  override def deserializeState(bytes: Array[Byte]): F[Either[Throwable, UpdateWithFeeChainState]] =
    Async[F].delay(Deserializers.deserializeState(bytes))

  override def deserializeUpdate(bytes: Array[Byte]): F[Either[Throwable, UpdateWithFee]] =
    Async[F].delay(Deserializers.deserializeUpdate(bytes))

  override def deserializeBlock(bytes: Array[Byte]): F[Either[Throwable, Signed[DataApplicationBlock]]] =
    Async[F].delay(Deserializers.deserializeBlock(bytes)(dataDecoder.asInstanceOf[Decoder[DataUpdate]]))

  override def dataEncoder: Encoder[UpdateWithFee] = implicitly[Encoder[UpdateWithFee]]

  override def dataDecoder: Decoder[UpdateWithFee] = implicitly[Decoder[UpdateWithFee]]

  override def routes(implicit context: Context): HttpRoutes[F] = HttpRoutes.empty

  override def signedDataEntityDecoder: EntityDecoder[F, Signed[UpdateWithFee]] = circeEntityDecoder

  override def calculatedStateEncoder: Encoder[CalculatedUpdateWithFeeState] = implicitly[Encoder[CalculatedUpdateWithFeeState]]

  override def calculatedStateDecoder: Decoder[CalculatedUpdateWithFeeState] = implicitly[Decoder[CalculatedUpdateWithFeeState]]

  override def getCalculatedState(implicit context: Context): F[(SnapshotOrdinal, CalculatedUpdateWithFeeState)] =
    calculatedStateService
      .getCalculatedState
      .flatTap(s => logger.debug(s"getCalculatedState\n${s.asJson.spaces2}"))

  override def setCalculatedState(ordinal: SnapshotOrdinal, state: CalculatedUpdateWithFeeState)(implicit context: Context): F[Boolean] =
    logger.debug(s"setCalculatedState: ${ordinal.show} ${state.asJson.spaces2}") >>
      calculatedStateService.setCalculatedState(ordinal, state)

  override def hashCalculatedState(state: CalculatedUpdateWithFeeState)(implicit context: Context): F[Hash] =
    calculatedStateService.hashCalculatedState(state)

  override def serializeCalculatedState(state: CalculatedUpdateWithFeeState): F[Array[Byte]] =
    Async[F].delay(Serializers.serializeCalculatedState(state))

  override def deserializeCalculatedState(bytes: Array[Byte]): F[Either[Throwable, CalculatedUpdateWithFeeState]] =
    Async[F].delay(Deserializers.deserializeCalculatedState(bytes))
}
