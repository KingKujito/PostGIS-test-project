package models

import java.math.BigInteger
import java.sql.Time

import scalikejdbc._

case class Teetime(time: Time, facility: BigInteger) {

}

object Teetime extends DBOperator[Teetime] with Countable[Teetime] with Creatable.Simple[Teetime] {
  val field = sqls"teetime"
  val valuenames = sqls"time_, facility"
  def values(teetime: Teetime) = sqls"${teetime.time},${teetime.facility.longValueExact}"

  def apply(rs: WrappedResultSet): Teetime = Teetime(
    time      = rs.time("time_"),
    facility  = rs.bigInt("facility")
  )
}