package models

import java.math.BigInteger

import scalikejdbc._

case class Person(
                   name: String,
                   override val id : Option[BigInteger] = None
                 ) extends WithId {

}

//TODO make an abstraction layer for this stuff
object Person {
  val field = sqls"person"
  def apply (rs: WrappedResultSet): Person = Person(
    id     = rs.bigIntOpt("id"),
    name   = rs.string("name")
  )

  def create(person: Person): Either[String, Int] = DB localTx { implicit session =>
    if(person.id.isDefined) {
      val s = s"This $field has a defined id. Are you trying to insert an already existing instance?"
      println(Console.RED+s+Console.RESET)
      Left(s)
    }
    else
      Right(sql"INSERT INTO $field (name) VALUES (${person.name})".update().apply())
  }

  def getAll: List[Person] = DB readOnly { implicit session =>
    sql"SELECT * FROM $field".map(rs => Person(rs)).list().apply()
  }

  def getById(id : BigInt): Option[Person] = DB readOnly { implicit session =>
    sql"SELECT * FROM $field WHERE id = $id".map(rs => Person(rs)).single().apply()
  }

  def getByName(name : String): Option[Person] = DB readOnly { implicit session =>
    sql"SELECT * FROM $field WHERE name = $name".map(rs => Person(rs)).single().apply()
  }

  //TODO test geographical lookup and ordering
  def getWithinRadius(long: Float, lat: Float, radius: Int): List[Person] = DB readOnly { implicit session =>
    sql"""
    SELECT * FROM $field, person_rel_location, location
    WHERE person.id = person_rel_location.person AND location.id = person_rel_location AND
    ((point($long, $lat) <@> location.longlat) * 1.60934) < $radius
    ORDER BY (point($long, $lat) <@> location.longlat)
    LIMIT 20
      """.map(rs => Person(rs)).list().apply()
  }

  def count: Option[Int] = DB readOnly { implicit session =>
    sql"SELECT COUNT(*) FROM $field".map(_.int("count")).single().apply()
  }

}