package com.my.nft_example.shared_data

import java.net.{MalformedURLException, URISyntaxException, URL}

object Utils {

  @throws[MalformedURLException]
  @throws[URISyntaxException]
  def isValidURL(url: String): Boolean = try {
    new URL(url).toURI
    true
  } catch {
    case _: MalformedURLException =>
      false
    case _: URISyntaxException =>
      false
  }
}

