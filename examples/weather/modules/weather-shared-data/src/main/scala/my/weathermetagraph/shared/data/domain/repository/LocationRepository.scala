package my.weathermetagraph.shared.data.domain.repository

import cats.effect.Async
import doobie.Transactor
import doobie.implicits._
import my.weathermetagraph.shared.data.domain.Location._

class LocationRepository[F[_]: Async](
  implicit xa: Transactor[F]) {

  def insert(
    location: LocationRecord
  ): F[Boolean] =
    sql"""
      INSERT INTO locations (id, name, region, country)
      VALUES (${location.id}, ${location.name}, ${location.region}, ${location.country})
    """.update.run.map(_ > 0).transact(xa)

  def findById(
    locationId: String
  ): F[Option[LocationRecord]] =
    sql"SELECT id, name, region, country FROM locations WHERE id = $locationId"
      .query[LocationRecord]
      .option
      .transact(xa)

  def findByName(
    locationName: String
  ): F[Option[LocationRecord]] =
    sql"SELECT id, name, region, country FROM locations WHERE name = $locationName"
      .query[LocationRecord]
      .option
      .transact(xa)

  def list: F[List[LocationRecord]] =
    sql"SELECT id, name, region, country FROM locations".query[LocationRecord].to[List].transact(xa)
}
