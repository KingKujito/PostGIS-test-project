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

//TODO make an abstraction layer for this stuff
object Location {
  val field = sqls"location"

  def create(location: Location): Either[String, Int] = DB localTx { implicit session =>
    if(location.id.isDefined) {
      val s = s"This $field has a defined id. Are you trying to insert an already existing instance?"
      println(Console.RED+s+Console.RESET)
      Left(s)
    }
    else
      Right(sql"INSERT INTO $field (longlat, geog) VALUES ((point(${location.long},${location.lat})), ${location.geog}::geography)".update().apply())
  }

  def count: Option[Int] = DB readOnly { implicit session =>
    sql"SELECT COUNT(*) FROM $field".map(_.int("count")).single().apply()
  }

}