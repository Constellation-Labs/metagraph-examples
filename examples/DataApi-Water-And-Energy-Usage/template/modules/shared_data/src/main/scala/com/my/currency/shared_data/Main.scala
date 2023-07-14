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

  /* This validation is necessary to avoid doubling the update on the state. When we send data to be updated,
   * we create blocks on the L1 layer, these blocks are sent to the L0 layer. To run the consensus, the L1 layer
   * needs the 3 nodes, and these nodes could send blocks with the same update to the L0 layer. This timestamp
   * validation prevents doubling the state on update checking this, if the state already was updated with that timestamp,
   * we should not update again.
  * */
  def validateEnergyTimestamp(maybeState: Option[AggregatedUsage], energy: EnergyUsage): DataApplicationValidationErrorOr[Unit] = {
    maybeState.map { total =>
      if (energy.timestamp > total.energy.timestamp && energy.usage > 0L)
        ().validNec
      else
        EnergyUpdateOutdated.asInstanceOf[DataApplicationValidationError].invalidNec
    }.getOrElse(().validNec)
  }

  /* This validation is necessary to avoid doubling the update on the state. When we send data to be updated,
   * we create blocks on the L1 layer, these blocks are sent to the L0 layer. To run the consensus, the L1 layer
   * needs the 3 nodes, and these nodes could send blocks with the same update to the L0 layer. This timestamp
   * validation prevents doubling the state on update checking this, if the state already was updated with that timestamp,
   * we should not update again.
  * */
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
  * This will be the schema of the update body, in this example a JSON like this:
  * { "address": "DAG...", "energyUsage":{"usage": 10, "timestamp": 10}, "waterUsage":{"usage": 11, "timestamp": 12} }
  */
  @derive(decoder, encoder)
  case class Update(address: Address, energyUsage: Option[EnergyUsage], waterUsage: Option[WaterUsage]) extends DataUpdate

  /*
  * This will be the schema of the State, in this example a JSON like this:
  * { "devices" : { "DAG8py4LY1sr8ZZM3aryeP85NuhgsCYcPKuhhbw6": { "waterUsage": { "usage": 10, "timestamp": 10 }, "energyUsage": { "usage": 100, "timestamp": 21 } } } }
  */
  @derive(decoder, encoder)
  case class State(devices: Map[Address, AggregatedUsage]) extends DataState

  /*
  * This function will do the validation of the provided body at the L1 layer. This validation will be done without
  * considering the previous State and the proofs. In this example, we're validating if the provided usage of the
  * Water and Energy is greater than 0, if not, we should return an error.
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

  /*
  * This function will validate the provided body at the L0 layer. Different from the validateUpdate function,
  * this function will consider the previous state and the proofs. In this example, we are checking if the provided
  * address is the same as the address that signed the message, on proofs. We also check if the provided timestamp
  * is greater than the last timestamp of the provided device. Different from validateUpdate, this function will not
  * return an error to sending user/device, but it will be shown on the logs if the message was rejected for validation reasons.
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

  /*
  * After the validations, this function is the one that effectively will update the State. We will get the list of
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

  /*
  * This function will do the serialization of the State. In other words, we want to transform the object State into
  * a JSON. For that, in this example, we are calling the function asJson.deepDropNullValues.noSpaces. This function
  * will transform to JSON and will remove all the null values and the spaces, so it should be a single line of JSON
  * without spaces and null values.
  */
  def serializeState(state: State): IO[Array[Byte]] = IO {
    println("Serialize state event received")
    println(state.asJson.deepDropNullValues.noSpaces)
    state.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
  }

  /*
  * This function will deserialize the State. In other words, we want to transform the JSON into an object State
  */
  def deserializeState(bytes: Array[Byte]): IO[Either[Throwable, State]] = IO {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[State]
    }
  }

  /**
   * This function will do the serialization of the Update message. In other words, we want to transform the object
   * Update into a JSON. For that, in this example, we are calling the function asJson.deepDropNullValues.noSpaces.
   * This function will transform to JSON and will remove all the null values and the spaces, so it should be a single
   * line of JSON without spaces and null values. It's very important to know that this function generates a JSON in
   * one order, each field has its position. When you'll send update messages be sure to serialize your messages exactly
   * equal to this part before signing, even the order of fields is important, the JSON message should be exactly equal
   * to the serialized JSON here
   */
  def serializeUpdate(update: Update): IO[Array[Byte]] = IO {
    println("Updated event received")
    println(update.asJson.deepDropNullValues.noSpaces)
    update.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
  }

  /*
  * This function will deserialize the Update. In other words, we want to transform the JSON into an object Update
  */
  def deserializeUpdate(bytes: Array[Byte]): IO[Either[Throwable, Update]] = IO {
    parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
      json.as[Update]
    }
  }

  def dataEncoder: Encoder[Update] = deriveEncoder

  def dataDecoder: Decoder[Update] = deriveDecoder
}