package com.my.currency.l0

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits.catsSyntaxValidatedIdBinCompat0
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, parser}
import io.circe.syntax.EncoderOps
import org.tessellation.BuildInfo
import org.tessellation.currency.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.{BaseDataApplicationL0Service, DataApplicationL0Service, DataApplicationValidationError, DataState, DataUpdate}
import org.tessellation.currency.l0.CurrencyL0App
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.security.SecurityProvider
import org.tessellation.security.signature.Signed

import java.nio.charset.StandardCharsets
import java.util.UUID
object Acme {
  @derive(decoder, encoder)
  sealed trait Usage extends DataUpdate {
    val value: Long
  }

  @derive(decoder, encoder)
  case class EnergyUsage(value: Long) extends Usage

  @derive(decoder, encoder)
  case class WaterUsage(value: Long) extends Usage

  @derive(decoder, encoder)
  case class State(totalEnergyUsage: Long, totalWaterUsage: Long) extends DataState

  case object EnergyNotPositive extends DataApplicationValidationError {
    val message = "energy usage must be positive"
  }

  case object WaterNotPositive extends DataApplicationValidationError {
    val message = "water usage must be positive"
  }

  val energy: DataApplicationL0Service[IO, Usage, State] = new DataApplicationL0Service[IO, Usage, State] {
    override def genesis: State = {
      State(0, 0)
    }

    def validateData(oldState: State, updates: NonEmptyList[Signed[Usage]]): IO[DataApplicationValidationErrorOr[Unit]] =
      IO {
        updates
          .map(_.value)
          .map {
            case update: EnergyUsage =>
              if (update.value > 0L) ().validNec else EnergyNotPositive.asInstanceOf[DataApplicationValidationError].invalidNec
            case update: WaterUsage =>
              if (update.value > 0L) ().validNec else WaterNotPositive.asInstanceOf[DataApplicationValidationError].invalidNec
          }
          .reduce
      }

    def validateUpdate(update: Usage): IO[DataApplicationValidationErrorOr[Unit]] = IO {
      update match {
        case u: EnergyUsage =>
          if (u.value > 0L) ().validNec else EnergyNotPositive.asInstanceOf[DataApplicationValidationError].invalidNec
        case u: WaterUsage =>
          if (u.value > 0L) ().validNec else WaterNotPositive.asInstanceOf[DataApplicationValidationError].invalidNec
      }
    }

    def combine(oldState: State, updates: NonEmptyList[Signed[Usage]]): IO[State] =
      IO {
        updates.foldLeft(oldState) { (acc, update) =>
          update match {
            case Signed(EnergyUsage(usage), _) => State(acc.totalEnergyUsage + usage, acc.totalWaterUsage)
            case Signed(WaterUsage(usage), _) => State(acc.totalEnergyUsage, acc.totalWaterUsage + usage)
          }
        }
      }

    def serializeState(state: State): IO[Array[Byte]] = IO {
      println("Serialize state event received")
      println(state.asJson.noSpaces)
      state.asJson.noSpaces.getBytes(StandardCharsets.UTF_8)
    }

    def deserializeState(bytes: Array[Byte]): IO[Either[Throwable, State]] = IO {
      parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
        json.as[State]
      }
    }

    def serializeUpdate(update: Usage): IO[Array[Byte]] = IO {
      println("Updated event received")
      println(update.asJson.noSpaces)
      update.asJson.noSpaces.getBytes(StandardCharsets.UTF_8)
    }

    def deserializeUpdate(bytes: Array[Byte]): IO[Either[Throwable, Usage]] = IO {
      parser.parse(new String(bytes, StandardCharsets.UTF_8)).flatMap { json =>
        json.as[Usage]
      }
    }

    def dataEncoder: Encoder[Usage] = deriveEncoder

    def dataDecoder: Decoder[Usage] = deriveDecoder
  }
}

  object Main
    extends CurrencyL0App(
      "custom-project-l0",
      "custom-project L0 node",
      ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
      version = BuildInfo.version
    ) {
    def dataApplication: Option[BaseDataApplicationL0Service[IO]] = Option(BaseDataApplicationL0Service(Acme.energy))

    def rewards(implicit sp: SecurityProvider[IO]) = None
  }

