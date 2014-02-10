package org.qirx.nomqondo.factories

import org.qirx.nomqondo.api.Failure
import org.qirx.nomqondo.api.Result
import org.qirx.nomqondo.api.Success
import org.qirx.nomqondo.api.CaseViolation

abstract class Pattern[T](messagePrefix: String, pattern: String) {

  def apply(value: String): T

  def from(value: String): Result[T, Invalid] =
    if (value matches pattern) Success(apply(value))
    else Failure(Invalid(value))

  case class Invalid(value: String)
    extends CaseViolation(messagePrefix + ".invalid", Seq(value, pattern))
}