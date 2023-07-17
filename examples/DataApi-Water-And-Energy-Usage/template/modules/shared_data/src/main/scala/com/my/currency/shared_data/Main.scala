package com.my.currency.shared_data

import cats.data.NonEmptyList
import cats.effect.IO
import com.my.currency.shared_data.Errors.{EmptyUpdate, EnergyUpdateOutdated, EnergyNotPositive, InvalidAddress, WaterUpdateOutdated, WaterNotPositive}
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
import com.my.currency.shared_data.Validations.{validateEnergyTimestamp, validateEnergyUsageUpdate, validateProvidedAddress, validateWaterTimestamp, validateWaterUsageUpdate}
import monocle.syntax.all._
import org.tessellation.schema.address.Address
import org.tessellation.security.SecurityProvider

import java.nio.charset.StandardCharsets

object Errors {
  case object EnergyNotPositive extends DataApplicationValidationError {
    val message = "Energy usage must be positive"
  }

  case object EnergyUpdateOutdated extends DataApplicationValidationError {
    val message = "Energy update older than latest update timestamp, rejecting"
  }

  case object WaterNotPositive extends DataApplicationValidationError {
    val message = "Water usage must be positive"
  }

  case object WaterUpdateOutdated extends DataApplicationValidationError {
    val message = "Water update older than latest update timestamp, rejecting"
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

  /** 
    * This validation prevents old updates from being accepted after a newer update has already been processed. 
    * There is also a case where old blocks can be sent to L0 consensus by different nodes with duplicate values
    * which can result in "double spends". This validation provides enough uniqueness to prevent that scenario. 
    * In other implementations, a unique field of some sort should be provided to validate whether an update has been 
    * already included in state or not. 
    */
  def validateEnergyTimestamp(maybeState: Option[AggregatedUsage], energy: EnergyUsage): DataApplicationValidationErrorOr[Unit] = {
    maybeState.map { total =>
      if (energy.timestamp > total.energy.timestamp && energy.usage > 0L)
        ().validNec
      else
        EnergyUpdateOutdated.asInstanceOf[DataApplicationValidationError].invalidNec
    }.getOrElse(().validNec)
  }

  /** 
    * This validation provides the same assurance as the `validateEnergyTimestamp` for WaterUsage. See comment above for details.
    */
  def validateWaterTimestamp(maybeState: Option[AggregatedUsage], water: WaterUsage): DataApplicationValidationErrorOr[Unit] =
    maybeState.map { total =>
      if (water.timestamp > total.energy.timestamp && water.usage > 0L)
        ().validNec
      else
        WaterUpdateOutdated.asInstanceOf[DataApplicationValidationError].invalidNec
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

  /*
  * This will be the schema of the update body, in this example an object like this:
  * { "address": "DAG...", "energyUsage":{"usage": 10, "timestamp": 10}, "waterUsage":{"usage": 11, "timestamp": 12} }
  */
  @derive(decoder, encoder)
  case class Update(address: Address, energyUsage: Option[EnergyUsage], waterUsage: Option[WaterUsage]) extends DataUpdate

  /*
  * This will be the schema of the State, in this example an object like this:
  * { "devices" : { "DAG8py4LY1sr8ZZM3aryeP85NuhgsCYcPKuhhbw6": { "waterUsage": { "usage": 10, "timestamp": 10 }, "energyUsage": { "usage": 100, "timestamp": 21 } } } }
  */
  @derive(decoder, encoder)
  case class State(devices: Map[Address, AggregatedUsage]) extends DataState

  /**
    * This method will do the validation of the provided body on the L1 layer. This validation will be done without
    * considering the previous State and the proofs. In this example, we're validating if the provided usage of the
    * Water and Energy is greater than 0, if not, we should return an error. Errors returned here will be sent to the 
    * client as the message portion of a 500 response.
    */
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

  /**
    * This method valides the update on the L0 layer. This method has access to the previous state and the proofs (request signatures). 
    * In this example, we are checking if the provided address is the same as the address that signed the message based on proofs. 
    * We also check if the provided timestamp is greater than the last timestamp of the provided device. 
    * 
    * Returning an error from this method will not return any error message to the client because the response would have already been sent on the L1
    * layer. Errors returned here will show up in the L0 logs. 
    */
  def validateData(oldState: State, updates: NonEmptyList[Signed[Update]])(implicit sp: SecurityProvider[IO]): IO[DataApplicationValidationErrorOr[Unit]] = {
    val validateUpdates = updates.traverse { update =>
      validateUpdate(update.value)
    }.map(_.reduce)

    val validateAddress = updates.traverse { update =>
        update.proofs
          .map(_.id)
          .toList
          .traverse(_.toAddress[IO])
          .map(validateProvidedAddress(_, update.value.address))
      }.map(_.reduce)


    val validateTimestamp = IO {
      updates.map { signedUpdate =>
        val update = signedUpdate.value
        val energyUsage = update.energyUsage.getOrElse(EnergyUsage.empty)
        val waterUsage = update.waterUsage.getOrElse(WaterUsage.empty)
        val address = update.address

        if (energyUsage.timestamp > 0 && waterUsage.timestamp > 0) {
          validateEnergyTimestamp(oldState.devices.get(address), energyUsage).productR(validateWaterTimestamp(oldState.devices.get(address), waterUsage))
        } else if (energyUsage.timestamp > 0) {
          validateEnergyTimestamp(oldState.devices.get(address), energyUsage)
        } else if (waterUsage.timestamp > 0) {
          validateWaterTimestamp(oldState.devices.get(address), waterUsage)
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

  /**
    * After validations have run, this function will update the State. We will get the list of
    * updates here and iterate over them. In this case, we take the provided address, then we search on the State to check
    * if we already have some information about this address. If we have some information about this device we will
    * concatenate with the current information the new values provided, e.g if we provide the address DAG...X and one update
    * of 10 on their water usage, and let's say that the state already contains this address and the amount of 20 on their
    * water usage, so this method will update the state to have 30 on the water usage. If the provided address does not
    * exist in the state, we will fill in the provided values without concatenating.
    */
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

  /**
    * This method will serialize the State. It accepts the State object, transforms it to a JSON string with no
    * whitespace, then converts to a byte array
    */
  def serializeState(state: State): IO[Array[Byte]] = IO {
    println("Serialize state event received")
    println(state.asJson.deepDropNullValues.noSpaces)
    state.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
  }

  /**
    * This method will deserialize the State from the byte array produced in `serializeState` 
    * by parsing it as JSON befor converting to the State object.
    */
  def deserializeState(bytes: Array[Byte]): IO[Either[Throwable, State]] = IO {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[State]
    }
  }

  /**
    * This method serializes the update `value` field to a byte array from the Update object. 
    * The update is serialized before checking proof validity.
    * 
    * This method processes JSON as a string so the order of the keys needs to be consistent. When you 
    * send updates be sure to serialize your messages exactly equal to this part before signing.
    * The JSON message should be exactly equal to the serialized JSON here.
    */
  def serializeUpdate(update: Update): IO[Array[Byte]] = IO {
    println("Updated event received")
    println(update.asJson.deepDropNullValues.noSpaces)
    update.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
  }

  /*
  * This function will deserialize the update byte array into the Update object. 
  */
  def deserializeUpdate(bytes: Array[Byte]): IO[Either[Throwable, Update]] = IO {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[Update]
    }
  }

  def dataEncoder: Encoder[Update] = deriveEncoder

  def dataDecoder: Decoder[Update] = deriveDecoder
}