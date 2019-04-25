package models

import scalikejdbc._

trait StandardGetters[A] extends Countable[A]{
  this : DBOperator[A] =>

  def apply(rs: WrappedResultSet) : A

  def getAll: List[A] = DB readOnly { implicit session =>
    sql"SELECT * FROM $field".map(rs => apply(rs)).list().apply()
  }

  def getById(id : BigInt): Option[A] = DB readOnly { implicit session =>
    sql"SELECT * FROM $field WHERE id = $id".map(rs => apply(rs)).single().apply()
  }
}
