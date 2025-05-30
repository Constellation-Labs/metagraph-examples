package com.my.data_l1

import cats.effect.Async
import cats.syntax.all._

import com.my.shared_data.schema.Updates.TodoUpdate
import com.my.shared_data.schema.{CalculatedState, OnChain}

import io.constellationnetwork.currency.dataApplication._
import io.constellationnetwork.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import io.constellationnetwork.metagraph_sdk.MetagraphCommonService
import io.constellationnetwork.metagraph_sdk.syntax.all.L1ContextOps
import org.http4s._

object DataL1Service {

  def make[F[+_]: Async]: F[BaseDataApplicationL1Service[F]] =
    for {
      validator <- Async[F].pure(DataL1Validator.make[F])
      dataApplicationL1Service = makeBaseApplicationL1Service(validator)
    } yield dataApplicationL1Service

  private def makeBaseApplicationL1Service[F[+_]: Async](
    validator: DataL1Validator[F, TodoUpdate, OnChain]
  ): BaseDataApplicationL1Service[F] =
    BaseDataApplicationL1Service[F, TodoUpdate, OnChain, CalculatedState](
      new MetagraphCommonService[F, TodoUpdate, OnChain, CalculatedState, L1NodeContext[F]]
        with DataApplicationL1Service[F, TodoUpdate, OnChain, CalculatedState] {

        override def validateUpdate(
          update: TodoUpdate
        )(implicit context: L1NodeContext[F]): F[DataApplicationValidationErrorOr[Unit]] =
          context.getOnChainState[OnChain].flatMap {
            _.fold(
              err => err.invalidNec[Unit].pure[F],
              onchain => validator.verify(onchain, update)
            )
          }

        override def routes(implicit context: L1NodeContext[F]): HttpRoutes[F] =
          new DataL1CustomRoutes[F].public
      }
    )
}
