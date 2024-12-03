package com.my.nft.shared_data.schema

import org.tessellation.schema.address.Address

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

@derive(decoder, encoder)
case class NFTCollection(
  id:                    String,
  owner:                 Address,
  name:                  String,
  creationDateTimestamp: Long,
  nfts:                  Map[Long, NFT]
)
