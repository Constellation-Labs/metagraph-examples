package com.my.nft_example.shared_data

import cats.data.NonEmptyList
import cats.effect.IO
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataApplicationValidationError, DataState, DataUpdate, L1NodeContext}
import org.tessellation.security.signature.Signed
import cats.syntax.all._
import com.my.nft_example.shared_data.Combiners.{combineMintCollection, combineMintNFT, combineTransferCollection, combineTransferNFT}
import com.my.nft_example.shared_data.Errors.{CouldNotGetLatestCurrencySnapshot, CouldNotGetLatestState}
import com.my.nft_example.shared_data.Utils.{customStateDeserialization, customStateSerialization, customUpdateDeserialization, customUpdateSerialization}
import com.my.nft_example.shared_data.Validations.{mintCollectionValidations, mintNFTValidations, mintNFTValidationsWithSignature, transferCollectionValidations, transferCollectionValidationsWithSignature, transferNFTValidations, transferNFTValidationsWithSignature}
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.tessellation.schema.address.Address
import org.tessellation.security.SecurityProvider

object Data {
  @derive(decoder, encoder)
  sealed trait NFTUpdate extends DataUpdate

  @derive(decoder, encoder)
  case class MintCollection(name: String, description: String) extends NFTUpdate

  @derive(decoder, encoder)
  case class MintNFT(owner: Address, collectionId: String, nftId: Long, uri: String, name: String, description: String, metadata: Map[String, String]) extends NFTUpdate

  @derive(decoder, encoder)
  case class TransferCollection(fromAddress: Address, toAddress: Address, collectionId: String) extends NFTUpdate

  @derive(decoder, encoder)
  case class TransferNFT(fromAddress: Address, toAddress: Address, collectionId: String, nftId: Long) extends NFTUpdate

  @derive(decoder, encoder)
  sealed trait CollectionState {
    val id: String
    val owner: Address
    val name: String
    val creationDateTimestamp: Long
    val nfts: Map[Long, NFTState]
  }

  @derive(decoder, encoder)
  case class Collection(id: String, owner: Address, name: String, creationDateTimestamp: Long, nfts: Map[Long, NFTState]) extends CollectionState

  @derive(decoder, encoder)
  sealed trait NFTState {
    val id: Long
    val collectionId: String
    val owner: Address
    val uri: String
    val name: String
    val description: String
    val creationDateTimestamp: Long
    val metadata: Map[String, String]
  }

  @derive(decoder, encoder)
  case class NFT(id: Long, collectionId: String, owner: Address, uri: String, name: String, description: String, creationDateTimestamp: Long, metadata: Map[String, String]) extends NFTState

  @derive(decoder, encoder)
  case class State(collections: Map[String, CollectionState]) extends DataState

  def validateUpdate(update: NFTUpdate)(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val lastCurrencySnapshot = context.getLastCurrencySnapshot
    lastCurrencySnapshot.map(_.get.data).flatMap {
      case Some(state) =>
        val currentState = customStateDeserialization(state)
        currentState match {
          case Left(_) => IO.pure(CouldNotGetLatestState.asInstanceOf[DataApplicationValidationError].invalidNec)
          case Right(state) =>
            update match {
              case mintCollection: MintCollection =>
                mintCollectionValidations(mintCollection, state)
              case mintNFT: MintNFT =>
                mintNFTValidations(mintNFT, state)
              case transferCollection: TransferCollection =>
                transferCollectionValidations(transferCollection, state)
              case transferNFT: TransferNFT =>
                transferNFTValidations(transferNFT, state)
            }

        }
      case None => IO.pure(CouldNotGetLatestCurrencySnapshot.asInstanceOf[DataApplicationValidationError].invalidNec)
    }
  }

  def validateData(oldState: State, updates: NonEmptyList[Signed[NFTUpdate]])(implicit sp: SecurityProvider[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    updates.traverse { signedUpdate =>
      signedUpdate.value match {
        case mintCollection: MintCollection =>
          mintCollectionValidations(mintCollection, oldState)
        case mintNFT: MintNFT =>
          mintNFTValidationsWithSignature(mintNFT, signedUpdate.proofs, oldState)
        case transferCollection: TransferCollection =>
          transferCollectionValidationsWithSignature(transferCollection, signedUpdate.proofs, oldState)
        case transferNFT: TransferNFT =>
          transferNFTValidationsWithSignature(transferNFT, signedUpdate.proofs, oldState)
      }
    }.map(_.reduce)
  }

  def combine(oldState: State, updates: NonEmptyList[Signed[NFTUpdate]])(implicit sp: SecurityProvider[IO]): IO[State] = {
    updates.foldLeftM(oldState) { (acc, signedUpdate) => {
      val update = signedUpdate.value
      update match {
        case mintCollection: MintCollection =>
          val collectionOwner = signedUpdate.proofs.map(_.id).toList.head.toAddress[IO]
          collectionOwner.map(address => combineMintCollection(mintCollection, acc, address))
        case mintNFT: MintNFT =>
          combineMintNFT(mintNFT, acc).pure[IO]
        case transferCollection: TransferCollection =>
          combineTransferCollection(transferCollection, acc).pure[IO]
        case transferNFT: TransferNFT =>
          combineTransferNFT(transferNFT, acc).pure[IO]
      }
    }
    }
  }

  def serializeState(state: State): IO[Array[Byte]] = IO {
    customStateSerialization(state)
  }

  def deserializeState(bytes: Array[Byte]): IO[Either[Throwable, State]] = IO {
    customStateDeserialization(bytes)
  }

  def serializeUpdate(update: NFTUpdate): IO[Array[Byte]] = IO {
    customUpdateSerialization(update)
  }

  def deserializeUpdate(bytes: Array[Byte]): IO[Either[Throwable, NFTUpdate]] = IO {
    customUpdateDeserialization(bytes)
  }

  def dataEncoder: Encoder[NFTUpdate] = deriveEncoder

  def dataDecoder: Decoder[NFTUpdate] = deriveDecoder
}