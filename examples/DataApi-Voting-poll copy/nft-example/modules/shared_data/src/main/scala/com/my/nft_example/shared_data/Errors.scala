package com.my.nft_example.shared_data

import org.tessellation.currency.dataApplication.DataApplicationValidationError

object Errors {

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
}

