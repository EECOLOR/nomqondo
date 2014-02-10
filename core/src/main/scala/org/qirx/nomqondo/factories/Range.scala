package org.qirx.nomqondo.factories

import org.qirx.nomqondo.api.Failure
import org.qirx.nomqondo.api.Result
import org.qirx.nomqondo.api.CaseViolation
import org.qirx.nomqondo.api.Success

abstract class Range[T](
    messagePrefix: String, min: Int, max: Int) {

  def apply(value: Int): T

  def from(value: Int): Result[T, Violation] =
    if (value < min) Failure(BelowMin(value))
    else if (value > max) Failure(AboveMax(value))
    else Success(apply(value))

  sealed abstract class Violation(value: Int, check: Int, message: String)
    extends CaseViolation(message, Seq(value, check))

  case class BelowMin(value: Int)
    extends Violation(value, min, messagePrefix + ".belowMin")

  case class AboveMax(value: Int)
    extends Violation(value, max, messagePrefix + ".aboveMax")
}