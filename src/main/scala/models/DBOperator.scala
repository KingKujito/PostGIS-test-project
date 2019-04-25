package models

import scalikejdbc.interpolation.SQLSyntax

trait DBOperator[A] {
  def field : SQLSyntax
}
