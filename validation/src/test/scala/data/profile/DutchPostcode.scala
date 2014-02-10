package data.profile

import org.qirx.nomqondo.factories.Value
import org.qirx.nomqondo.factories.Pattern
import org.qirx.nomqondo.common.NonEmptyString
import org.qirx.nomqondo.api.Result

case class DutchPostcode private(value:String) extends Value[String]

object DutchPostcode extends Pattern[DutchPostcode]("postcode", pattern = """\d{4} ?[A-Z]{2}""") {

  def from(value:NonEmptyString): Result[DutchPostcode, Invalid] =
    from(value:String)
}