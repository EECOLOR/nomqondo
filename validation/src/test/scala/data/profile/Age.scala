package data.profile

import org.qirx.nomqondo.factories.Range
import org.qirx.nomqondo.factories.Value

case class Age private (value: Int) extends Value[Int]
object Age extends Range[Age]("age", min = 18, max = 65)
