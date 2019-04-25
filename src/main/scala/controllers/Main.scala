package controllers

import models._
import scalikejdbc._
import utils._

object Main extends App {

  override def main(args: Array[String]): Unit = {

    println(s"${Console.BLUE}START\n--------${Console.RESET}")

    withConnection{ _ =>
      //make sure we're working in a clean environment
      cleanupTable
      //setup our schema
      initTables
      //setup our data
      DataGenerator.populateDb()
      //check if everything went well
      require(Person   .count.contains(DataGenerator.desiredEntries) &&
              Location .count.contains(DataGenerator.desiredEntries),
              "Your actual data does not seem to comply with the desired data. Please check your db and/or the code"
      )

      //then do this or whatever you want...
      println(Person.getAll)
    }

    println(s"${Console.BLUE}--------\nEND${Console.RESET}")

  }

  //just a little function I used to test postgres
  //TODO remove this
  def testing = {
    DB readOnly { implicit session =>
      sql"SELECT (point(0, 0) <@> point(20, 20)) * 1.60934 as dist_in_km".map(_.long("dist_in_km")).single.apply()
    }
  }

}
