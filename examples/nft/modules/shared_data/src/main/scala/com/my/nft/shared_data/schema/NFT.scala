package com.my.nft.shared_data.schema

import org.tessellation.schema.address.Address

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

@derive(decoder, encoder)
case class NFT(
  id:                    Long,
  collectionId:          String,
  owner:                 Address,
  uri:                   String,
  name:                  String,
  description:           String,
  creationDateTimestamp: Long,
  metadata:              Map[String, String]
)
