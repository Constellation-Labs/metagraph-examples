package my.weathermetagraph.shared.data.domain

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.tessellation.schema.address.Address

object Account {

  @derive(encoder, decoder)
  case class AccountState(
    address: Address,
    balance: Int)

}
