package com.my.shared_data.lib

import cats.effect.Async

import org.tessellation.currency.dataApplication.DataApplicationValidationError
import org.tessellation.routes.internal.{InternalUrlPrefix, PublicRoutes}

import eu.timepit.refined.auto._
import io.circe.Encoder
import io.circe.syntax.EncoderOps
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.CORS
import org.http4s.{HttpRoutes, Response}

abstract class MetagraphPublicRoutes[F[_]: Async] extends Http4sDsl[F] with PublicRoutes[F] {

  protected val routes: HttpRoutes[F]

  override lazy val public: HttpRoutes[F] =
    CORS.policy
      .withAllowCredentials(false)
      .httpRoutes(routes)

  override protected def prefixPath: InternalUrlPrefix = "/"

  protected def prepareResponse[T: Encoder](response: Either[DataApplicationValidationError, T]): F[Response[F]] =
    response match {
      case Left(ex)     => BadRequest(ex.message)
      case Right(value) => Ok(value.asJson)
    }
}
