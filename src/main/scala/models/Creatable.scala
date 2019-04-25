package models

import scalikejdbc._

trait Creatable[A <: WithId]{
  this : DBOperator[A] =>

  def valuenames          : SQLSyntax
  def values(a: A)        : SQLSyntax

  def create(i: A): Either[String, Int] = DB localTx { implicit session =>
    if(i.id.isDefined) {
      val s = s"This $field has a defined id. Are you trying to insert an already existing instance?"
      println(Console.RED+s+Console.RESET)
      Left(s)
    }
    else
      Right(sql"INSERT INTO $field ($valuenames) VALUES (${values(i)})".update().apply())
  }
}

object Creatable {
  trait Simple[A]{
    this : DBOperator[A] =>

    def valuenames          : SQLSyntax
    def values(a: A)        : SQLSyntax

    def create(i: A): Int = DB localTx { implicit session =>
      sql"INSERT INTO $field ($valuenames) VALUES (${values(i)})".update().apply()
    }
  }
}
