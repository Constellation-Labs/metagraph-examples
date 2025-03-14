package my.weathermetagraph.shared.data.domain.repository

import cats.effect.Async
import cats.implicits._
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}
import my.weathermetagraph.shared.data.domain.Weather._

import java.sql.Timestamp

class WeatherRepository[F[_]: Async](
  implicit xa: Transactor[F]) {

  def insert(
    record: WeatherRecord
  ): F[Boolean] =
    nonTransactionalInsert(record).map(_ > 0).transact(xa)

  private def nonTransactionalInsert(
    record: WeatherRecord
  ): ConnectionIO[Int] =
    sql"""
      INSERT INTO weather_records (id, snapshot_ordinal, location_id, temp_f, temp_c, condition, record_date)
      VALUES (${record.id}, ${record.snapshotOrdinal}, ${record.location.id}, ${record.tempF},
       ${record.tempC}, ${record.condition.toString}, ${Timestamp.from(record.recordDate)})
    """.update.run

  def insertAll(
    records: List[WeatherRecord]
  ): F[Boolean] =
    records.traverse_(nonTransactionalInsert).transact(xa).map(_ > 0)

  def findById(
    weatherId: String
  ): F[Option[WeatherRecord]] =
    sql"""
      SELECT w.id, w.snapshot_ordinal, l.id, l.name, l.region, l.country, w.temp_f, w.temp_c, w.condition, w.record_date
      FROM weather_records w
      INNER JOIN locations l ON w.location_id = l.id
      WHERE w.id = $weatherId
    """.query[WeatherRecord].option.transact(xa)

  def findLatestByLocation(
    locationName: String
  ): F[Option[WeatherRecord]] =
    sql"""
      SELECT w.id, w.snapshot_ordinal, l.id, l.name, l.region, l.country, w.temp_f, w.temp_c, w.condition, w.record_date
      FROM weather_records w
      INNER JOIN locations l ON w.location_id = l.id
      WHERE l.name = $locationName
      ORDER BY w.record_date DESC
      LIMIT 1
    """.query[WeatherRecord].option.transact(xa)

  def list: F[List[WeatherRecord]] =
    sql"""
      SELECT w.id, w.snapshot_ordinal, l.id, l.name, l.region, l.country, w.temp_f, w.temp_c, w.condition, w.record_date
      FROM weather_records w
      INNER JOIN locations l ON w.location_id = l.id
    """.query[WeatherRecord].to[List].transact(xa)
}
