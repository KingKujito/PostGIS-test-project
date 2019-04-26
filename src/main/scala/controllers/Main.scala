package controllers

import models._
import utils._

object Main extends App {
  //Select which extension you'd like to use.
  val extension : Extension = Earthdistance
  val generateData = true
  lazy val myCoordinates = DataScraper.getMyLocation

  override def main(args: Array[String]): Unit = {

    println(s"${Console.YELLOW}Note that a slow browser could cause this program to fail.${Console.RESET}")
    println(s"${Console.BLUE}START\n--------${Console.RESET}")

    withConnection{ _ =>
      if(generateData) {
        //make sure we're working in a clean environment
        cleanupTable
        //setup our schema
        initTables
        //setup our data
        DataGenerator.populateDb()
        //check if everything went well
        require(
          Person              .count.contains(DataGenerator.desiredEntries) &&
          PersonRelLocation   .count.contains(DataGenerator.desiredEntries) &&
          Location            .count.contains(DataGenerator.desiredEntries),
          "Your actual data does not seem to comply with the desired data. Please check your db and/or the code"
        )
      }
      //then do this or whatever you want...
      getTeetimesWithinRadius(
        myCoordinates.head,
        myCoordinates.reverse.head,
        750
      ).foreach(println)
    }

    println(s"${Console.BLUE}--------\nEND${Console.RESET}")

  }


}
