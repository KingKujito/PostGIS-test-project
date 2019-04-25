package models

import java.math.BigInteger

import scalikejdbc._

case class Facility(name: String, longtitude: BigDecimal, latitude: BigDecimal,
                    override val id : Option[BigInteger] = None) extends WithId {
  //constructor
  def this(name: String, location: Location) = this(name, location.long, location.lat)
}

object Facility extends DBOperator[Facility] with StandardGetters[Facility] with Creatable[Facility] {
  val field = sqls"facility"
  val valuenames = sqls"name, longtitude, latitude"
  def values(facility: Facility) = sqls"${facility.name},${facility.longtitude},${facility.latitude}"

  def apply(rs: WrappedResultSet): Facility = Facility(
    id          = rs.bigIntOpt("id"),
    name        = rs.string("name"),
    longtitude  = rs.bigDecimal("longtitude"),
    latitude    = rs.bigDecimal("latitude")
  )
}