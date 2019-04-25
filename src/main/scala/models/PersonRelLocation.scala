package models

import java.math.BigInteger

import scalikejdbc._

object PersonRelLocation
  extends DBOperator[(BigInteger, BigInteger)] with Countable[(BigInteger, BigInteger)] with Creatable.Simple[(BigInteger, BigInteger)] {
  val field = sqls"person_rel_location"
  val valuenames = sqls"person, location"
  def values(personLocation: (BigInteger, BigInteger)) =
    sqls"${personLocation._1.longValueExact},${personLocation._2.longValueExact}"

}

