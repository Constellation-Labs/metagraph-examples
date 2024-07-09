package com.my.reward_api.l0

import cats.data.NonEmptyList
import cats.effect.{Async, IO}
import cats.implicits.catsSyntaxApplicativeId
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineV
import eu.timepit.refined.types.numeric.PosLong
import io.circe.parser.decode
import org.tessellation.BuildInfo
import org.tessellation.currency.dataApplication.DataCalculatedState
import org.tessellation.currency.l0.CurrencyL0App
import org.tessellation.currency.schema.currency.{CurrencyIncrementalSnapshot, CurrencySnapshotStateProof}
import org.tessellation.node.shared.domain.rewards.Rewards
import org.tessellation.node.shared.infrastructure.consensus.trigger.ConsensusTrigger
import org.tessellation.node.shared.snapshot.currency.CurrencySnapshotEvent
import org.tessellation.schema.address.Address
import org.tessellation.schema.balance.Balance
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.schema.semver.{MetagraphVersion, TessellationVersion}
import org.tessellation.schema.transaction.{RewardTransaction, Transaction, TransactionAmount}
import org.tessellation.security.SecurityProvider
import org.tessellation.security.signature.Signed

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
      case x: Exception =>
        println(s"Error when fetching API: ${x.getMessage}")
        List[Address]()

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

  def make[F[_] : Async]: Rewards[F, CurrencySnapshotStateProof, CurrencyIncrementalSnapshot, CurrencySnapshotEvent] =
    (
      _: Signed[CurrencyIncrementalSnapshot],
      _: SortedMap[Address, Balance],
      _: SortedSet[Signed[Transaction]],
      _: ConsensusTrigger,
      _: Set[CurrencySnapshotEvent],
      _: Option[DataCalculatedState]
    ) => {

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

object Main
  extends CurrencyL0App(
    "custom-rewards-l0",
    "custom-rewards L0 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version),
    tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version)
  ) {

  override def rewards(implicit sp: SecurityProvider[IO]): Some[Rewards[IO, CurrencySnapshotStateProof, CurrencyIncrementalSnapshot, CurrencySnapshotEvent]] = Some(
    RewardsMintForEachAddressOnApi.make[IO]
  )
}