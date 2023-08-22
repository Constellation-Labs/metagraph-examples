package com.my.currency.data_l1

import cats.data.OptionT
import cats.effect.IO
import cats.implicits.catsSyntaxOption
import com.my.currency.shared_data.Data.deserializeState
import com.my.currency.shared_data.Types.{UpdateUsageTransaction, UsageState}
import com.my.currency.shared_data.Utils.customStateDeserialization
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.tessellation.currency.dataApplication.L1NodeContext
import org.tessellation.schema.address.Address
import io.circe.parser._

object CustomRoutes {
  private def getState(context: L1NodeContext[IO]) = {
    OptionT(context.getLastCurrencySnapshot)
      .flatMap(_.data.toOptionT)
      .flatMapF(deserializeState(_).map(_.toOption))
      .value
  }

  private def getMetagraphStateFromSnapshotOrdinal(ordinal: Long): Array[Byte] = {
    val headers: Iterable[(String, String)] = Iterable(("Accept", "application/json"))
    val response = requests.get(s"http://host.docker.internal:9400/snapshots/$ordinal", headers = headers)
    parse(response.text()) match {
      case Left(_) => throw new Exception("Error decoding JSON")
      case Right(value) =>
        value.hcursor.downField("value").downField("data").as[Array[Byte]] match {
          case Left(e) => throw new Exception(s"Error getting state on snapshot $ordinal: ${e.getMessage}")
          case Right(value) => value
        }
    }
  }

  private def getAddressTransactionsFromState(state: UsageState, address: Address): List[UpdateUsageTransaction] = {
    val addressTransactions = state.transactions.filter { case (_, transaction) => transaction.owner == address }
    addressTransactions.values.toList
  }

  private def getAllAddressTransactions(address: Address, ordinal: Long, transactions: List[UpdateUsageTransaction]): List[UpdateUsageTransaction] = {
    val state = getMetagraphStateFromSnapshotOrdinal(ordinal)
    val deserializedState = customStateDeserialization(state)
    deserializedState match {
      case Left(_) =>
        throw new Exception("State could not be deserialized")
      case Right(state) =>
        state.lastTxnRefs.get(address) match {
          case None =>
            transactions
          case Some(lastTxnRefs) =>
            if (lastTxnRefs.snapshotOrdinal != ordinal) {
              return getAllAddressTransactions(address, lastTxnRefs.snapshotOrdinal, transactions)
            }
            val allTransactions = transactions ++ getAddressTransactionsFromState(state, address)
            state.lastSnapshotRefs.get(address).map { value =>
              if (value.ordinal == 0) {
                return allTransactions
              }
              getAllAddressTransactions(address, value.ordinal, allTransactions)
            }.getOrElse(transactions)
        }
    }
  }

  def getAllDevices()(implicit context: L1NodeContext[IO]): IO[Response[IO]] = {
    getState(context).flatMap {
      case None => NotFound()
      case Some(value) =>
        Ok(value.devices)
    }
  }

  def getDeviceByAddress(address: Address)(implicit context: L1NodeContext[IO]): IO[Response[IO]] = {
    getState(context).flatMap {
      case None => NotFound()
      case Some(value) =>
        value.devices.get(address).map { value =>
          Ok(value)
        }.getOrElse(NotFound())
    }
  }

  def getDeviceTransactions(address: Address)(implicit context: L1NodeContext[IO]): IO[Response[IO]] = {
    context.getLastCurrencySnapshot.flatMap {
      case None => NotFound()
      case Some(currencySnapshot) =>
        val latestOrdinal = currencySnapshot.ordinal.value.value
        val allTransactions = getAllAddressTransactions(address, latestOrdinal, List.empty).sortBy(_.snapshotOrdinal)(Ordering[Long].reverse)
        Ok(allTransactions)
    }
  }
}