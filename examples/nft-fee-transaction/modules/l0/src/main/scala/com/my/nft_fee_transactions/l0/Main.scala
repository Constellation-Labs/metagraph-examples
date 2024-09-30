package com.my.nft_fee_transactions.l0

import cats.Applicative
import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import cats.syntax.all._
import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.option.catsSyntaxOptionId
import com.my.nft_fee_transactions.l0.custom_routes.CustomRoutes
import com.my.nft_fee_transactions.shared_data.LifecycleSharedFunctions
import com.my.nft_fee_transactions.shared_data.calculated_state.CalculatedStateService
import com.my.nft_fee_transactions.shared_data.deserializers.Deserializers
import com.my.nft_fee_transactions.shared_data.serializers.Serializers
import com.my.nft_fee_transactions.shared_data.types.Types._
import io.circe.{Decoder, Encoder}
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.{EntityDecoder, HttpRoutes}
import io.constellationnetwork.BuildInfo
import io.constellationnetwork.currency.dataApplication.Errors.{MissingFeeTransaction, NotEnoughFee}
import io.constellationnetwork.currency.dataApplication._
import io.constellationnetwork.currency.dataApplication.dataApplication._
import io.constellationnetwork.currency.l0.CurrencyL0App
import io.constellationnetwork.ext.cats.effect.ResourceIO
import io.constellationnetwork.schema.SnapshotOrdinal
import io.constellationnetwork.schema.cluster.ClusterId
import io.constellationnetwork.schema.semver.{MetagraphVersion, TessellationVersion}
import io.constellationnetwork.security.hash.Hash
import io.constellationnetwork.security.signature.Signed

import java.util.UUID

object Main
  extends CurrencyL0App(
    "currency-l0",
    "currency L0 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version),
    tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version)
  ) {
  private def makeBaseDataApplicationL0Service(
    calculatedStateService: CalculatedStateService[IO]
  ): BaseDataApplicationL0Service[IO] = BaseDataApplicationL0Service(new DataApplicationL0Service[IO, NFTUpdate, NFTUpdatesState, NFTUpdatesCalculatedState] {
    override def genesis: DataState[NFTUpdatesState, NFTUpdatesCalculatedState] =
      DataState(NFTUpdatesState(List.empty), NFTUpdatesCalculatedState(Map.empty))

    override def validateData(
      state  : DataState[NFTUpdatesState, NFTUpdatesCalculatedState],
      updates: NonEmptyList[Signed[NFTUpdate]]
    )(implicit context: L0NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] =
      LifecycleSharedFunctions.validateData[IO](state, updates)

    override def combine(
      state  : DataState[NFTUpdatesState, NFTUpdatesCalculatedState],
      updates: List[Signed[NFTUpdate]]
    )(implicit context: L0NodeContext[IO]): IO[DataState[NFTUpdatesState, NFTUpdatesCalculatedState]] =
      LifecycleSharedFunctions.combine[IO](state, updates)

    override def serializeState(
      state: NFTUpdatesState
    ): IO[Array[Byte]] =
      IO(Serializers.serializeState(state))

    override def serializeUpdate(
      update: NFTUpdate
    ): IO[Array[Byte]] =
      IO(Serializers.serializeUpdate(update))

    override def serializeBlock(
      block: Signed[DataApplicationBlock]
    ): IO[Array[Byte]] = IO(Serializers.serializeBlock(block)(dataEncoder.asInstanceOf[Encoder[DataUpdate]]))

    override def deserializeState(
      bytes: Array[Byte]
    ): IO[Either[Throwable, NFTUpdatesState]] =
      IO(Deserializers.deserializeState(bytes))

    override def deserializeUpdate(
      bytes: Array[Byte]
    ): IO[Either[Throwable, NFTUpdate]] =
      IO(Deserializers.deserializeUpdate(bytes))

    override def deserializeBlock(
      bytes: Array[Byte]
    ): IO[Either[Throwable, Signed[DataApplicationBlock]]] =
      IO(Deserializers.deserializeBlock(bytes)(dataDecoder.asInstanceOf[Decoder[DataUpdate]]))

    override def dataEncoder: Encoder[NFTUpdate] =
      implicitly[Encoder[NFTUpdate]]

    override def dataDecoder: Decoder[NFTUpdate] =
      implicitly[Decoder[NFTUpdate]]

    override def calculatedStateEncoder: Encoder[NFTUpdatesCalculatedState] =
      implicitly[Encoder[NFTUpdatesCalculatedState]]

    override def calculatedStateDecoder: Decoder[NFTUpdatesCalculatedState] =
      implicitly[Decoder[NFTUpdatesCalculatedState]]

    override def routes(implicit context: L0NodeContext[IO]): HttpRoutes[IO] =
      CustomRoutes[IO](calculatedStateService).public

    override def signedDataEntityDecoder: EntityDecoder[IO, Signed[NFTUpdate]] =
      circeEntityDecoder

    override def getCalculatedState(implicit context: L0NodeContext[IO]): IO[(SnapshotOrdinal, NFTUpdatesCalculatedState)] =
      calculatedStateService.getCalculatedState.map(calculatedState => (calculatedState.ordinal, calculatedState.state))

    override def setCalculatedState(
      ordinal: SnapshotOrdinal,
      state  : NFTUpdatesCalculatedState
    )(implicit context: L0NodeContext[IO]): IO[Boolean] =
      calculatedStateService.setCalculatedState(ordinal, state)

    override def hashCalculatedState(
      state: NFTUpdatesCalculatedState
    )(implicit context: L0NodeContext[IO]): IO[Hash] =
      calculatedStateService.hashCalculatedState(state)

    override def serializeCalculatedState(
      state: NFTUpdatesCalculatedState
    ): IO[Array[Byte]] =
      IO(Serializers.serializeCalculatedState(state))

    override def deserializeCalculatedState(
      bytes: Array[Byte]
    ): IO[Either[Throwable, NFTUpdatesCalculatedState]] =
      IO(Deserializers.deserializeCalculatedState(bytes))

    /**
     * Validates the fees for the incoming `dataUpdate` and optional `feeTransaction`.
     *
     * In this template, all `dataUpdate` transactions are accepted without fees, except for `MintCollection`.
     * If a `MintCollection` request is sent without a fee, the validation will fail. Similarly, if the
     * `feeTransaction` has an amount less than 10000 tokens, the validation will also fail, and the transaction will be rejected.
     *
     * The error types `NotEnoughFee` and `MissingFeeTransaction` are used to handle these cases, and they are part of the
     * Tessellation codebase.
     *
     * @param gsOrdinal The global snapshot ordinal.
     * @param dataUpdate The provided `dataUpdate` to be validated.
     * @param maybeFeeTransaction The optional `feeTransaction` associated with the `dataUpdate`.
     *
     * @return An `IO` containing whether the `dataUpdate` and optional `feeTransaction` are valid.
     */
    override def validateFee(
      gsOrdinal: SnapshotOrdinal
    )(dataUpdate: Signed[NFTUpdate], maybeFeeTransaction: Option[Signed[FeeTransaction]])(
      implicit context: L0NodeContext[IO], A: Applicative[IO]
    ): IO[DataApplicationValidationErrorOr[Unit]] = {
      maybeFeeTransaction match {
        case Some(feeTransaction) =>
          dataUpdate.value match {
            case _: MintCollection =>
              if (feeTransaction.value.amount.value.value < 10000)
                NotEnoughFee.invalidNec[Unit].pure[IO]
              else
                ().validNec[DataApplicationValidationError].pure[IO]
            case _ =>
              ().validNec[DataApplicationValidationError].pure[IO]
          }
        case None =>
          MissingFeeTransaction.invalidNec[Unit].pure[IO]
      }
    }
  })

  private def makeL0Service: IO[BaseDataApplicationL0Service[IO]] =
    CalculatedStateService.make[IO].map(makeBaseDataApplicationL0Service)

  override def dataApplication: Option[Resource[IO, BaseDataApplicationL0Service[IO]]] =
    makeL0Service.asResource.some
}
