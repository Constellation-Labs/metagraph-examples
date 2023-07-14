package com.my.currency.l0

import cats.data.NonEmptyList
import cats.effect.{Async, IO}
import cats.implicits.catsSyntaxApplicativeId
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.tessellation.BuildInfo
import org.tessellation.currency.dataApplication.BaseDataApplicationL0Service
import org.tessellation.currency.l0.CurrencyL0App
import org.tessellation.currency.schema.currency.{CurrencyBlock, CurrencyIncrementalSnapshot, CurrencySnapshotStateProof, CurrencyTransaction}
import org.tessellation.schema.address.Address
import org.tessellation.schema.balance.Balance
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.schema.transaction.{RewardTransaction, TransactionAmount}
import org.tessellation.sdk.domain.rewards.Rewards
import org.tessellation.security.SecurityProvider
import org.tessellation.security.signature.Signed
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineV
import eu.timepit.refined.types.numeric.PosLong
import org.tessellation.sdk.infrastructure.consensus.trigger.ConsensusTrigger
import io.circe.parser.decode

import java.util.UUID
import scala.collection.immutable.{SortedMap, SortedSet}

object RewardsMintForEachAddressOnApi {
  private def getRewardAddresses: List[Address] = {

    @derive(decoder, encoder)
    case class AddressTimeEntry(address: Address, date: String)

    try {
      //Using host.docker.internal as host because we will fetch this from a docker container to a API that is on local machine
      //You should replace to your url
      val response = requests.get("http://host.docker.internal:8000/addresses")
      val body = response.text()

      println("API response" + body)

      decode[List[AddressTimeEntry]](body) match {
        case Left(e) => throw e
        case Right(addressTimeEntries) => addressTimeEntries.map(_.address)
      }
    } catch {
      case x: Exception => {
        println(s"Error when fetching API: ${x.getMessage}")
        List[Address]()
      }
    }
  }

  private def getAmountPerWallet(addressCount: Int): PosLong = {
    val totalAmount: Long = 100_000_0000L
    val amountPerWallet: Either[String, PosLong] = refineV[Positive](totalAmount / addressCount)

    amountPerWallet.toOption match {
      case Some(amount) => amount
      case None =>
        println("Error getting amount per wallet")
        PosLong(1)
    }
  }

  def make[F[_] : Async ] =
    new Rewards[F, CurrencyTransaction, CurrencyBlock, CurrencySnapshotStateProof, CurrencyIncrementalSnapshot] {
      def distribute(
                      lastArtifact: Signed[CurrencyIncrementalSnapshot],
                      lastBalances: SortedMap[Address, Balance],
                      acceptedTransactions: SortedSet[Signed[CurrencyTransaction]],
                      trigger: ConsensusTrigger
                    ): F[SortedSet[RewardTransaction]] = {

        val rewardAddresses = getRewardAddresses
        val foo = NonEmptyList.fromList(rewardAddresses)

        foo match {
          case Some(addresses) =>
            val amountPerWallet = getAmountPerWallet(addresses.size)
            val rewardAddressesAsSortedSet = SortedSet(addresses.toList: _*)

            rewardAddressesAsSortedSet.map(address => {
              val txnAmount = TransactionAmount(amountPerWallet)
              RewardTransaction(address, txnAmount)
            }).pure[F]

          case None =>
            println("Could not find reward addresses")
            val nodes: SortedSet[RewardTransaction] = SortedSet.empty
            nodes.pure[F]
        }
      }
    }
}

object Main
  extends CurrencyL0App(
    "custom-rewards-l0",
    "custom-rewards L0 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    version = BuildInfo.version
  ) {

  def dataApplication: Option[BaseDataApplicationL0Service[IO]] = None

  def rewards(implicit sp: SecurityProvider[IO]) = Some(
    RewardsMintForEachAddressOnApi.make[IO]
  )
}