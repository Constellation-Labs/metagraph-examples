package com.my.nft.shared_data.errors

import cats.syntax.validated.catsSyntaxValidatedIdBinCompat0
import org.tessellation.currency.dataApplication.DataApplicationValidationError
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr

object Errors {
  type DataApplicationValidationType = DataApplicationValidationErrorOr[Unit]

  val valid: DataApplicationValidationType =
    ().validNec[DataApplicationValidationError]

  implicit class DataApplicationValidationTypeOps[E <: DataApplicationValidationError](err: E) {
    def invalid: DataApplicationValidationType =
      err.invalidNec[Unit]

    def unlessA(
      cond: Boolean
    ): DataApplicationValidationType =
      if (cond) valid else invalid

    def whenA(
      cond: Boolean
    ): DataApplicationValidationType =
      if (cond) invalid else valid
  }

  case object DuplicatedCollection extends DataApplicationValidationError {
    val message = "Duplicated collection"
  }

  case object InvalidNFTUri extends DataApplicationValidationError {
    val message = "NFT URI is invalid"
  }

  case object NFTAlreadyExists extends DataApplicationValidationError {
    val message = "NFT already exists"
  }

  case object NFTUriAlreadyExists extends DataApplicationValidationError {
    val message = "NFT URI already exists"
  }

  case object CollectionNotExists extends DataApplicationValidationError {
    val message = "Collection not exists"
  }

  case object NFTNotExists extends DataApplicationValidationError {
    val message = "NFT does not exists"
  }

  case object CollectionDoesNotBelongsToProvidedAddress extends DataApplicationValidationError {
    val message = "Collection does not belongs to provided address"
  }

  case object NFTDoesNotBelongsToProvidedAddress extends DataApplicationValidationError {
    val message = "NFT does not belongs to provided address"
  }

  case object CouldNotGetLatestCurrencySnapshot extends DataApplicationValidationError {
    val message = "Could not get latest currency snapshot!"
  }

  case object CouldNotGetLatestState extends DataApplicationValidationError {
    val message = "Could not get latest state!"
  }

  case object InvalidAddress extends DataApplicationValidationError {
    val message = "Provided address different than proof"
  }

  case class InvalidFieldSize(fieldName: String, maxSize: Long) extends DataApplicationValidationError {
    val message = s"Invalid field size: $fieldName, maxSize: $maxSize"
  }
}

