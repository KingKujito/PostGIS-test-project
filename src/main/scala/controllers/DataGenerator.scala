package controllers
import models._
import scalikejdbc._
import java.math.BigInteger

import scala.util.Random

/**
  * This object fills the database.
  */
object DataGenerator {
  //The amount of rows we want our db to contain
  val desiredEntries = 50

  /**
    * Put data for random people and random locations in the db, then randomly assign relations between them.
    */
  def populateDb(): Unit = DB localTx {implicit session =>
    //logic for data creation
    val remaining = desiredEntries - Person.count.getOrElse(0)
    val locations = desiredEntries - Location.count.getOrElse(0)
    val relations = desiredEntries - PersonRelLocation.count.getOrElse(0)

    randomPeople(remaining)
      .foreach(Person.create)

    randomLocations(locations)
      .foreach(Location.create)

    //logic for assigning each location to people randomly
    val indx = Random.shuffle((1 until (desiredEntries+1)).toList)
    for(i <- 1 to desiredEntries) yield PersonRelLocation.create(
      BigInteger.valueOf(i), BigInteger.valueOf(indx(i-1)))

    //checking if everything went as expected
    val p = Person.count.getOrElse(0)
    val l = Location.count.getOrElse(0)
    val r = PersonRelLocation.count.getOrElse(0)
    require(
      remaining == locations && remaining == relations,
      s"The tables are out of sync... p=$p,l=$l,prl=$r...\nDeleting all tables and data...${utils.cleanupTable; ""} Please retry..."
    )
  }

  def generateRandomFloat  : Float   = Random.nextFloat()

  /**
    * Scrape the web for random names and create people out of them
    */
  def randomPeople(amount: Int) : List[Person] = {
    val names = DataScraper.scrape(amount)
    (for (i <- 0 until amount) yield Person(names(i))).toList
  }

  def randomLocations(amount: Int) : List[Location] =
    (for (_ <- 0 until  amount) yield Location(generateRandomFloat*360-180, generateRandomFloat*180-90)).toList

}
