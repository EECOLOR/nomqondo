package org.qirx.nomqondo.factories

import org.qirx.nomqondo.api.Violation
import org.qirx.nomqondo.api.Result
import scala.util.Try
import org.qirx.nomqondo.api.Failure
import org.qirx.nomqondo.api.Success
import org.qirx.nomqondo.api.CaseViolation
import org.qirx.nomqondo.common.NonEmptyString

abstract class Catching[O](messagePrefix: String, convert: String => O) {

  def from(value: NonEmptyString): Result[O, ConversionFailed] =
    Try(convert(value))
      .map(Success[O])
      .recover {
        case _: Throwable =>
          Failure(ConversionFailed(value))
      }
      .get

  case class ConversionFailed(value: String)
    extends CaseViolation(messagePrefix + ".conversionFailed", Seq(value))
}