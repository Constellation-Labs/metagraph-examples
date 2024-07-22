package com.my.shared_data.lib

import cats.effect.Sync
import cats.implicits.toFunctorOps

import org.tessellation.json.JsonSerializer

import io.circe.jawn.JawnParser
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Printer}

object JsonBinaryCodec {

  def apply[F[_]: JsonSerializer]: JsonSerializer[F] = implicitly

  def forSync[F[_]: Sync]: F[JsonSerializer[F]] = {
    def printer = Printer(dropNullValues = true, indent = "", sortKeys = true)

    forSync[F](printer)
  }

  def forSync[F[_]: Sync](printer: Printer): F[JsonSerializer[F]] =
    Sync[F].delay {
      new JsonSerializer[F] {
        override def serialize[A: Encoder](content: A): F[Array[Byte]] =
          Sync[F].delay(content.asJson.printWith(printer).getBytes("UTF-8"))

        override def deserialize[A: Decoder](content: Array[Byte]): F[Either[Throwable, A]] =
          Sync[F]
            .delay(content)
            .map(JawnParser(false).decodeByteArray[A](_))
      }
    }
}
