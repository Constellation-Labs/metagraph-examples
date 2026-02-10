package com.my.shared_data.lib

import io.constellationnetwork.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr

trait UpdateValidator[F[_], U, T] {
  def verify(state: T, update: U): F[DataApplicationValidationErrorOr[Unit]]
}
