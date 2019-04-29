import controllers.DataGenerator
import models._
import scalikejdbc.config.DBs
import scalikejdbc._

package object utils {

  /**
    * Makes sure the DB connections are handled properly.
    */
  def withConnection(unit: Any => Unit): Unit = {
    DBs.setupAll()
    try unit.apply()
    finally DBs.closeAll()
  }

  /**
    * setup the schema
    */
  def initTables: Int = DB localTx { implicit session =>
    sql"""
     CREATE SEQUENCE IF NOT EXISTS location_id_seq;
     CREATE SEQUENCE IF NOT EXISTS person_id_seq;
     CREATE SEQUENCE IF NOT EXISTS facility_id_seq;


     CREATE TABLE IF NOT EXISTS facility (
              name character varying(50) COLLATE pg_catalog."default" NOT NULL,
              longitude decimal,
              latitude decimal,
              id bigint NOT NULL DEFAULT nextval('facility_id_seq'::regclass),
              CONSTRAINT facility_pkey PRIMARY KEY (id)
          )
          WITH ( OIDS = FALSE )
          TABLESPACE pg_default;


     CREATE TABLE IF NOT EXISTS teetime (
              time_ time NOT NULL,
              facility bigint NOT NULL,
              CONSTRAINT facility FOREIGN KEY (facility)
                  REFERENCES public.facility (id) MATCH SIMPLE
                  ON UPDATE NO ACTION
                  ON DELETE CASCADE
          )
          WITH ( OIDS = FALSE )
          TABLESPACE pg_default;


     CREATE TABLE IF NOT EXISTS location (
         longlat point,
         geog geography,
         id bigint NOT NULL DEFAULT nextval('location_id_seq'::regclass),
         CONSTRAINT location_pkey PRIMARY KEY (id)
     )
     WITH ( OIDS = FALSE )
     TABLESPACE pg_default;


     CREATE TABLE IF NOT EXISTS person (
         name character varying(50) COLLATE pg_catalog."default" NOT NULL,
         id bigint NOT NULL DEFAULT nextval('person_id_seq'::regclass),
         CONSTRAINT person_pkey PRIMARY KEY (id)
     )
     WITH ( OIDS = FALSE )
     TABLESPACE pg_default;


     CREATE TABLE IF NOT EXISTS person_rel_location (
         person bigint NOT NULL,
         location bigint NOT NULL,
         CONSTRAINT location FOREIGN KEY (location)
             REFERENCES public.location (id) MATCH SIMPLE
             ON UPDATE NO ACTION
             ON DELETE CASCADE,
         CONSTRAINT person FOREIGN KEY (person)
             REFERENCES public.person (id) MATCH SIMPLE
             ON UPDATE NO ACTION
             ON DELETE CASCADE
     )
     WITH ( OIDS = FALSE )
     TABLESPACE pg_default;
    """.update().apply()
  }

  /**
    * Remove all data and clean up the schema (also resets sequences).
    */
  def cleanupTable: Int = DB localTx { implicit session =>
    sql"""
     DROP SEQUENCE IF EXISTS location_id_seq CASCADE;
     DROP SEQUENCE IF EXISTS person_id_seq CASCADE;
     DROP SEQUENCE IF EXISTS facility_id_seq CASCADE;

     DROP TABLE IF EXISTS person_rel_location;
     DROP TABLE IF EXISTS location;
     DROP TABLE IF EXISTS person;
     DROP TABLE IF EXISTS teetime;
     DROP TABLE IF EXISTS facility;
    """.update().apply()
  }

  /**
    * Gets people, locations and distances within radius from (equator, meridian).
    * @param radius in kilometers
    */
  def getNearMiddle(radius: Int, limit: Int = 20, extension: Extension = defaultExtension)
  : List[(Person, Location, Float)] = DB readOnly { implicit session =>
    sql"""
         SELECT
         	person.name AS name,
          person.id AS personid,
         	location.longlat AS position,
          location.id AS locationid,
             ${distanceQuery(0,0, extension=extension)} AS dist_in_km
         	FROM person, person_rel_location, location
             WHERE person.id = person_rel_location.person AND location.id = person_rel_location.person AND
             ${withinQuery(0,0, radius, extension=extension)}
             ORDER BY dist_in_km
             LIMIT $limit
    """.map{rs =>
      val position = rs.string("position").replaceAll("""[\(\)]""", "").split(",")
      (Person(rs.string("name"), rs.bigIntOpt("personid")),
      Location(
        position.head.toFloat,
        position.reverse.head.toFloat,
        rs.bigIntOpt("locationid")
      ),
      rs.float("dist_in_km")
    )}.list().apply()
  }

  def distanceQuery(lat: Float, long: Float,
                    table: SQLSyntax = sqls"location", extension: Extension = defaultExtension): SQLSyntax = extension match {
    case PostGIS         => sqls"ST_Distance(('SRID=4326;POINT('||$lat||' '||$long||')')::geography, $table.geog)/1000"
    case Earthdistance   => sqls"((point($lat, $long) <@> $table.longlat)*1.60934)"}

  def withinQuery(lat: Float, long: Float, radius: Int,
                  table: SQLSyntax = sqls"location", extension: Extension = defaultExtension): SQLSyntax = extension match {
    case PostGIS         => sqls"ST_DWithin(('SRID=4326;POINT('||$lat||' '||$long||')')::geography, $table.geog, $radius*1000)"
    case Earthdistance   => sqls"((point($lat, $long) <@> $table.longlat)*1.60934) < $radius"}


  /**
    * Gets facilities, teetimes and distances within radius from (long, lat).
    * @param radius in kilometers
    */
  def getTeetimesWithinRadius(lat: Float, long: Float, radius: Int, limit: Int = 20, extension: Extension = defaultExtension)
  : List[(Facility, Teetime, Float)] = DB readOnly { implicit session =>
    sql"""SELECT *, ${distanceQuery2(lat, long,  extension=extension)} AS dist_in_km
    FROM facility, teetime
    WHERE ${withinQuery2(lat, long, radius, extension=extension)} AND teetime.facility = facility.id
    ORDER BY dist_in_km LIMIT $limit""".map{rs =>
      (Facility.apply(rs), Teetime.apply(rs), rs.float("dist_in_km"))
    }.list().apply()
  }

  def distanceQuery2(lat: Float, long: Float,
                    table: SQLSyntax = sqls"facility", extension: Extension = defaultExtension): SQLSyntax = extension match {
    case PostGIS         =>
      sqls"ST_Distance(('SRID=4326;POINT('||$lat||' '||$long||')')::geography, ('SRID=4326;POINT('||$table.latitude||' '||$table.longitude||')')::geography)/1000"
    case Earthdistance   => sqls"((point($lat,$long) <@> point($table.latitude, $table.longitude))*1.60934)"}

  def withinQuery2(lat: Float, long: Float, radius: Int,
                  table: SQLSyntax = sqls"facility", extension: Extension = defaultExtension): SQLSyntax = extension match {
    case PostGIS         =>
      sqls"ST_DWithin(('SRID=4326;POINT('||$lat||' '||$long||')')::geography, ('SRID=4326;POINT('||$table.latitude||' '||$table.longitude||')')::geography, $radius*1000)"
    case Earthdistance   => sqls"((point($lat,$long) <@> point($table.latitude, $table.longitude))*1.60934) < $radius"}



  /**
    * Gets facilities, teetimes and distances within radius from (long, lat).
    * @param radius in kilometers
    */
  def benchmarker(lat: Float, long: Float, radius: Int, extension: Extension = defaultExtension)
  : String = DB readOnly { implicit session =>
    sql"""
         EXPLAIN ANALYZE
    SELECT ${distanceQuery2(lat, long,  extension=extension)} AS dist_in_km
    FROM facility
    WHERE ${withinQuery2(lat, long, radius, extension=extension)}
    ORDER BY dist_in_km""".map{rs =>
      rs.string("QUERY PLAN")
    }.list().apply().apply(6) //change to apply(11) for many rows
  }

  def benchmark (benchTries : Int = 25): Unit = {
    val benchE = for(_ <- 1 to benchTries) yield benchmarker(0,0,30000, extension = Earthdistance)
    val benchP = for(_ <- 1 to benchTries) yield benchmarker(0,0,30000, extension = PostGIS)

    println(s"${Console.BLUE}For ${DataGenerator.desiredEntries} rows\n------")
    println(s"${Console.GREEN}Earthdistance performance${Console.RESET}")
    benchE.foreach(println)

    println(s"${Console.GREEN}Average: ${average(
      benchE.map(_.replaceAll("[a-zA-z: ]", "").toFloat).toList
    )}${Console.RESET} ms\n")

    println(s"${Console.GREEN}PostGIS performance${Console.RESET}")
    benchP.foreach(println)

    println(s"${Console.GREEN}Average: ${average(
      benchP.map(_.replaceAll("[a-zA-z: ]", "").toFloat).toList
    )}${Console.RESET} ms\n")
  }

  def average (list : List[Float]): Float = list.sum / list.length
}
