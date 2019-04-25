package models

import java.math.BigInteger

trait WithId {
  def id : Option[BigInteger] = None
}
