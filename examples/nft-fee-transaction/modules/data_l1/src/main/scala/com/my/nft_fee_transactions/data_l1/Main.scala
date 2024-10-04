package com.my.nft_fee_transactions.data_l1

import cats.Applicative
import cats.effect.{IO, Resource}
import cats.syntax.all._
import cats.syntax.option.catsSyntaxOptionId
import com.my.nft_fee_transactions.shared_data.types.Types._
import com.my.nft_fee_transactions.shared_data.LifecycleSharedFunctions
import com.my.nft_fee_transactions.shared_data.deserializers.Deserializers
import com.my.nft_fee_transactions.shared_data.serializers.Serializers
import eu.timepit.refined.auto._
import eu.timepit.refined.types.numeric.NonNegLong
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import io.constellationnetwork.BuildInfo
import io.constellationnetwork.currency.dataApplication.Errors.{MissingFeeTransaction, NotEnoughFee}
import io.constellationnetwork.currency.dataApplication._
import io.constellationnetwork.currency.dataApplication.dataApplication._
import io.constellationnetwork.currency.l1.CurrencyL1App
import io.constellationnetwork.currency.schema.EstimatedFee
import io.constellationnetwork.ext.cats.effect.ResourceIO
import io.constellationnetwork.schema.SnapshotOrdinal
import io.constellationnetwork.schema.address.Address
import io.constellationnetwork.schema.balance.Amount
import io.constellationnetwork.schema.cluster.ClusterId
import io.constellationnetwork.schema.semver.{MetagraphVersion, TessellationVersion}
import io.constellationnetwork.security.signature.Signed

import java.util.UUID

object Main
  extends CurrencyL1App(
    "currency-data_l1",
    "currency data L1 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version),
    tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version)
  ) {
  private def makeBaseDataApplicationL1Service: BaseDataApplicationL1Service[IO] = BaseDataApplicationL1Service(new DataApplicationL1Service[IO, NFTUpdate, NFTUpdatesState, NFTUpdatesCalculatedState] {
    override def validateUpdate(
      update: NFTUpdate
    )(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] =
      LifecycleSharedFunctions.validateUpdate[IO](update)

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
    ): IO[Array[Byte]] =
      IO(Serializers.serializeBlock(block)(dataEncoder.asInstanceOf[Encoder[DataUpdate]]))

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

    override def routes(implicit context: L1NodeContext[IO]): HttpRoutes[IO] =
      HttpRoutes.empty

    override def signedDataEntityDecoder: EntityDecoder[IO, Signed[NFTUpdate]] =
      circeEntityDecoder

    override def serializeCalculatedState(
      state: NFTUpdatesCalculatedState
    ): IO[Array[Byte]] =
      IO(Serializers.serializeCalculatedState(state))

    override def deserializeCalculatedState(
      bytes: Array[Byte]
    ): IO[Either[Throwable, NFTUpdatesCalculatedState]] =
      IO(Deserializers.deserializeCalculatedState(bytes))

    /**
     * Estimates the fee for an `NFTUpdate` transaction.
     *
     * Based on the type of `NFTUpdate`, this method returns a predefined fee and destination address.
     * Supported transaction types include:
     * - `MintCollection`: 10000 tokens (datum)
     * - `MintNFT`: 110000 tokens (datum)
     * - `TransferCollection`: 120000 tokens (datum)
     * - `TransferNFT`: 130000 tokens (datum)
     *
     * @param gsOrdinal The current global snapshot ordinal.
     * @param update The `NFTUpdate` transaction.
     * @param context The node's L1 context.
     * @param A Applicative instance for IO.
     * @return An `IO` containing the estimated fee and destination address.
     */
    override def estimateFee(gsOrdinal: SnapshotOrdinal)(update: NFTUpdate)(implicit context: L1NodeContext[IO], A: Applicative[IO]): IO[EstimatedFee] = {
      update match {
        case _: MintCollection => IO.pure(EstimatedFee(Amount(NonNegLong(10000)), Address("DAG88C9WDSKH451sisyEP3hAkgCKn5DN72fuwjfQ")))
        case _: MintNFT => IO.pure(EstimatedFee(Amount(NonNegLong(110000)), Address("DAG88C9WDSKH451sisyEP3hAkgCKn5DN72fuwjfQ")))
        case _: TransferCollection => IO.pure(EstimatedFee(Amount(NonNegLong(120000)), Address("DAG88C9WDSKH451sisyEP3hAkgCKn5DN72fuwjfQ")))
        case _: TransferNFT => IO.pure(EstimatedFee(Amount(NonNegLong(130000)), Address("DAG88C9WDSKH451sisyEP3hAkgCKn5DN72fuwjfQ")))
      }
    }

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
      implicit context: L1NodeContext[IO], A: Applicative[IO]
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

  private def makeL1Service: IO[BaseDataApplicationL1Service[IO]] =
    IO.delay(makeBaseDataApplicationL1Service)

  override def dataApplication: Option[Resource[IO, BaseDataApplicationL1Service[IO]]] =
    makeL1Service.asResource.some
}
