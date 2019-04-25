package models

import java.math.BigInteger

import scalikejdbc._

case class Location (
                      long: Float,
                      lat:  Float,
                      override val id : Option[BigInteger] = None
                    ) extends WithId {
  lazy val geog = s"SRID=4326;POINT($long $lat)"
}

object Location extends DBOperator[Location] with Countable[Location] with Creatable[Location] {
  val field = sqls"location"
  val valuenames = sqls"longlat, geog"
  def values(location: Location) = sqls"(point(${location.long},${location.lat})), ${location.geog}::geography"

}