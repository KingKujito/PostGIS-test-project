package models

import java.math.BigInteger

import scalikejdbc._

case class Person(
                   name: String,
                   override val id : Option[BigInteger] = None
                 ) extends WithId {

}

object Person extends DBOperator[Person] with StandardGetters[Person] with Creatable[Person] {
  val field = sqls"person"
  val valuenames = sqls"name"
  def values(person: Person) = sqls"${person.name}"

  def apply (rs: WrappedResultSet): Person = Person(
    id     = rs.bigIntOpt("id"),
    name   = rs.string("name")
  )

  def getByName(name : String): Option[Person] = DB readOnly { implicit session =>
    sql"SELECT * FROM $field WHERE name = $name".map(rs => Person(rs)).single().apply()
  }

  /**
    * Gets people within radius.
    * @param radius in kilometers
    */
  def getWithinRadius(long: Float, lat: Float, radius: Int, extension: Extension = defaultExtension)
  : List[Person] = DB readOnly { implicit session =>
    val distance = extension match {
      case PostGIS         => sqls"ST_Distance('SRID=4326;POINT($long $lat)'::geography, location.geog)"
      case Earthdistance   => sqls"(point($long, $lat) <@> location.longlat)"}

    val within   = extension match {
      case PostGIS         => sqls"ST_DWithin('SRID=4326;POINT($long $lat)'::geography, location.geog, $radius*1000)"
      case Earthdistance   => sqls"((point($long, $lat) <@> location.longlat) * 1.60934) < $radius"}

    sql"""
    SELECT * FROM $field, person_rel_location, location
    WHERE person.id = person_rel_location.person AND location.id = person_rel_location.person AND
    $within
    ORDER BY $distance
    LIMIT 20
      """.map(rs => Person(rs)).list().apply()
  }
}