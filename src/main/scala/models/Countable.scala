package models

import scalikejdbc._

trait Countable[A] {
  this : DBOperator[A] =>
  def count: Option[Int] = DB readOnly { implicit session =>
    sql"SELECT COUNT(*) FROM $field".map(_.int("count")).single().apply()
  }
}