package com.my.currency.shared_data

import cats.data.NonEmptyList
import cats.effect.IO
import com.my.currency.shared_data.Errors.{EmptyUpdate, EnergyAlreadyUpdated, EnergyNotPositive, InvalidAddress, WaterAlreadyUpdated, WaterNotPositive}
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, parser}
import io.circe.syntax.EncoderOps
import org.http4s.HttpRoutes
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataApplicationValidationError, DataState, DataUpdate}
import org.tessellation.security.signature.Signed
import cats.syntax.all._
import com.my.currency.shared_data.Data.{AggregatedUsage, EnergyUsage, WaterUsage}
import com.my.currency.shared_data.Validations.{validateEnergyTimestampToNoDoubling, validateEnergyUsageUpdate, validateProvidedAddress, validateWaterTimestampToNoDoubling, validateWaterUsageUpdate}
import monocle.syntax.all._
import org.tessellation.schema.address.Address
import org.tessellation.security.SecurityProvider

import java.nio.charset.StandardCharsets

object Errors {
  case object EnergyNotPositive extends DataApplicationValidationError {
    val message = "energy usage must be positive"
  }

  case object EnergyAlreadyUpdated extends DataApplicationValidationError {
    val message = "Energy already updated, skipping"
  }

  case object WaterNotPositive extends DataApplicationValidationError {
    val message = "water usage must be positive"
  }

  case object WaterAlreadyUpdated extends DataApplicationValidationError {
    val message = "Energy already updated, skipping"
  }

  case object EmptyUpdate extends DataApplicationValidationError {
    val message = "Provided an empty update"
  }

  case object InvalidAddress extends DataApplicationValidationError {
    val message = "Provided address different than proof"
  }
}

object Validations {
  def validateEnergyUsageUpdate(energy: EnergyUsage): DataApplicationValidationErrorOr[Unit] =
    if (energy.usage > 0L && energy.timestamp > 0L)
      ().validNec
    else
      EnergyNotPositive.asInstanceOf[DataApplicationValidationError].invalidNec

  def validateWaterUsageUpdate(water: WaterUsage): DataApplicationValidationErrorOr[Unit] =
    if (water.usage > 0L && water.timestamp > 0L)
      ().validNec
    else
      WaterNotPositive.asInstanceOf[DataApplicationValidationError].invalidNec

  def validateEnergyTimestampToNoDoubling(maybeState: Option[AggregatedUsage], energy: EnergyUsage): DataApplicationValidationErrorOr[Unit] = {
    maybeState.map { total =>
      if (energy.timestamp > total.energy.timestamp && energy.usage > 0L)
        ().validNec
      else
        EnergyAlreadyUpdated.asInstanceOf[DataApplicationValidationError].invalidNec
    }.getOrElse(().validNec)
  }

  def validateWaterTimestampToNoDoubling(maybeState: Option[AggregatedUsage], water: WaterUsage): DataApplicationValidationErrorOr[Unit] =
    maybeState.map { total =>
      if (water.timestamp > total.energy.timestamp && water.usage > 0L)
        ().validNec
      else
        WaterAlreadyUpdated.asInstanceOf[DataApplicationValidationError].invalidNec
    }.getOrElse(().validNec)

  def validateProvidedAddress(proofAddresses: List[Address], address: Address): DataApplicationValidationErrorOr[Unit] = {
    if (proofAddresses.contains(address)) {
      ().validNec
    } else {
      InvalidAddress.asInstanceOf[DataApplicationValidationError].invalidNec
    }
  }
}

object Data {
  @derive(decoder, encoder)
  sealed trait Usage extends DataUpdate {
    val timestamp: Long
    val usage: Long
  }

  @derive(decoder, encoder)
  case class EnergyUsage(usage: Long, timestamp: Long) extends Usage

  object EnergyUsage {
    def empty: EnergyUsage = EnergyUsage(0L, 0L)
  }

  @derive(decoder, encoder)
  case class WaterUsage(usage: Long, timestamp: Long) extends Usage

  object WaterUsage {
    def empty: WaterUsage = WaterUsage(0L, 0L)
  }

  @derive(decoder, encoder)
  case class AggregatedUsage(energy: EnergyUsage, water: WaterUsage)

  object AggregatedUsage {
    def empty: AggregatedUsage = AggregatedUsage(EnergyUsage.empty, WaterUsage.empty)
  }

  @derive(decoder, encoder)
  case class Update(address: Address, energyUsage: Option[EnergyUsage], waterUsage: Option[WaterUsage]) extends DataUpdate

  @derive(decoder, encoder)
  case class State(devices: Map[Address, AggregatedUsage]) extends DataState

  def validateData(oldState: State, updates: NonEmptyList[Signed[Update]])(implicit sp: SecurityProvider[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateUpdates = updates.traverse { update =>
      validateUpdate(update.value)
    }.map(_.reduce)

    val validateAddress = updates.traverse { update =>
        val p = update.proofs
          .map(_.id)
          .toList
          .traverse(_.toAddress[IO])
          .map(validateProvidedAddress(_, update.value.address))
        p
      }.map(_.reduce)


    val validateTimestamp = IO {
      updates.map { signedUpdate =>
        val update = signedUpdate.value
        val energyUsage = update.energyUsage.getOrElse(EnergyUsage.empty)
        val waterUsage = update.waterUsage.getOrElse(WaterUsage.empty)
        val address = update.address

        if (energyUsage.timestamp > 0 && waterUsage.timestamp > 0) {
          validateEnergyTimestampToNoDoubling(oldState.devices.get(address), energyUsage).productR(validateWaterTimestampToNoDoubling(oldState.devices.get(address), waterUsage))
        } else if (energyUsage.timestamp > 0) {
          validateEnergyTimestampToNoDoubling(oldState.devices.get(address), energyUsage)
        } else if (waterUsage.timestamp > 0) {
          validateWaterTimestampToNoDoubling(oldState.devices.get(address), waterUsage)
        } else {
          ().validNec
        }
      }.reduce
    }

    for {
      validatedUpdates <- validateUpdates
      validatedAddress <- validateAddress
      validatedTimestamp <- validateTimestamp
    } yield validatedUpdates.productR(validatedAddress).productR(validatedTimestamp)

  }

  def validateUpdate(update: Update): IO[DataApplicationValidationErrorOr[Unit]] = {
    IO {
      val energyUsage = update.energyUsage.getOrElse(EnergyUsage.empty)
      val waterUsage = update.waterUsage.getOrElse(WaterUsage.empty)

      if (energyUsage.timestamp == 0 && waterUsage.timestamp == 0) {
        EmptyUpdate.asInstanceOf[DataApplicationValidationError].invalidNec
      } else if (energyUsage.timestamp > 0 && waterUsage.timestamp > 0) {
        validateEnergyUsageUpdate(energyUsage).productR(validateWaterUsageUpdate(waterUsage))
      } else if (energyUsage.timestamp > 0) {
        validateEnergyUsageUpdate(energyUsage)
      } else if (waterUsage.timestamp > 0) {
        validateWaterUsageUpdate(waterUsage)
      } else {
        ().validNec
      }
    }
  }

  def combine(oldState: State, updates: NonEmptyList[Signed[Update]]): IO[State] = IO {
    updates.foldLeft(oldState) { (acc, signedUpdate) => {
      val update = signedUpdate.value
      val energyUsage = update.energyUsage.getOrElse(EnergyUsage.empty)
      val waterUsage = update.waterUsage.getOrElse(WaterUsage.empty)
      val address = update.address

      if (energyUsage.timestamp > 0 && waterUsage.timestamp > 0) {
        val currentState = acc.devices.getOrElse(address, AggregatedUsage.empty)
        val newState = currentState
          .focus(_.energy)
          .modify { current =>
            EnergyUsage(current.usage + energyUsage.usage, energyUsage.timestamp)
          }
          .focus(_.water)
          .modify { current =>
            WaterUsage(current.usage + waterUsage.usage, waterUsage.timestamp)
          }

        acc.focus(_.devices).modify(_.updated(address, newState))
      } else if (energyUsage.timestamp > 0) {
        val currentState = acc.devices.getOrElse(address, AggregatedUsage.empty)
        val newState = currentState.focus(_.energy).modify { current =>
          EnergyUsage(current.usage + energyUsage.usage, energyUsage.timestamp)
        }
        acc.focus(_.devices).modify(_.updated(address, newState))
      } else if (waterUsage.timestamp > 0) {
        val currentState = acc.devices.getOrElse(address, AggregatedUsage.empty)
        val newState = currentState.focus(_.water).modify { current =>
          WaterUsage(current.usage + waterUsage.usage, waterUsage.timestamp)
        }
        acc.focus(_.devices).modify(_.updated(address, newState))
      } else {
        acc
      }
    }
    }
  }

  def serializeState(state: State): IO[Array[Byte]] = IO {
    println("Serialize state event received")
    println(state.asJson.deepDropNullValues.noSpaces)
    state.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
  }

  def deserializeState(bytes: Array[Byte]): IO[Either[Throwable, State]] = IO {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[State]
    }
  }

  def serializeUpdate(update: Update): IO[Array[Byte]] = IO {
    println("Updated event received")
    println(update.asJson.deepDropNullValues.noSpaces)
    update.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
  }

  def deserializeUpdate(bytes: Array[Byte]): IO[Either[Throwable, Update]] = IO {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[Update]
    }
  }

  def routes: HttpRoutes[IO] = HttpRoutes.empty

  def dataEncoder: Encoder[Update] = deriveEncoder

  def dataDecoder: Decoder[Update] = deriveDecoder
}