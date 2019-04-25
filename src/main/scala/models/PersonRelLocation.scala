package models

import java.math.BigInteger

import scalikejdbc._

//TODO make an abstraction layer for this stuff
object PersonRelLocation {
  val field = sqls"person_rel_location"

  def create(person: BigInteger, location: BigInteger): Int = DB localTx { implicit session =>
    sql"INSERT INTO $field (person, location) VALUES (${person.longValueExact},${location.longValueExact})".update().apply()
  }

  def count: Option[Int] = DB readOnly { implicit session =>
    sql"SELECT COUNT(*) FROM $field".map(_.int("count")).single().apply()
  }

}

