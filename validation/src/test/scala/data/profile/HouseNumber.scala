package data.profile

import org.qirx.nomqondo.common.NonEmptyString
import org.qirx.nomqondo.factories.Value
import org.qirx.nomqondo.factories.Range
import org.qirx.nomqondo.api.SimpleViolation
import org.qirx.nomqondo.api.Result
import org.qirx.nomqondo.api.Violation
import org.qirx.nomqondo.api.Failure
import org.qirx.nomqondo.api.Success
import org.qirx.nomqondo.common

case class HouseNumber private (number: HouseNumber.Number, suffix: Option[NonEmptyString])

object HouseNumber {

  def from(value: String): Result[HouseNumber, Violation] = {
    val HouseNumberParts = """(\d+)(.*)""".r

    value match {
      case HouseNumberParts(number, suffix) =>
        createFromStrings(number, suffix.trim)

      case _ => Failure(Invalid)
    }
  }

  case object Invalid extends SimpleViolation("houseNumber.invalid")

  case class Number private (value: Int) extends Value[Int]
  object Number extends Range[Number]("houseNumber.number", min = 1, max = scala.Int.MaxValue)

  private def createFromStrings(numberString: String, suffixString: String) = {
    val suffix = NonEmptyString.asOption(suffixString)
    val number = NonEmptyString.from(numberString)
      .flatMap(common.Number.Int.from)
      .flatMap(Number.from)

    number.fold(
      violations => Failure(Invalid +: violations),
      number => Success(HouseNumber(number, suffix)))
  }
}