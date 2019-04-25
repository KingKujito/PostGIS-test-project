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
     CREATE SEQUENCE IF NOT EXISTS public.location_id_seq;
     CREATE SEQUENCE IF NOT EXISTS public.person_id_seq;


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

     DROP TABLE IF EXISTS person_rel_location;
     DROP TABLE IF EXISTS location;
     DROP TABLE IF EXISTS person;
    """.update().apply()
  }

}
