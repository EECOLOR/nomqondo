package org.qirx.nomqondo.common

import org.qirx.nomqondo.api.SimpleViolation
import org.qirx.nomqondo.api.Success
import org.qirx.nomqondo.api.Result
import org.qirx.nomqondo.api.Failure
import org.qirx.nomqondo.factories.Value

case class NonEmptyString private (value: String) extends Value[String]

object NonEmptyString {
  type Empty = Empty.type

  def from(value: String): Result[NonEmptyString, Empty] =
    if (value.isEmpty) Failure(Empty)
    else Success(NonEmptyString(value))

  def asOption(value:String):Option[NonEmptyString] =
    from(value).fold(_ => None, Some.apply)

  case object Empty extends SimpleViolation("nonEmptyString.empty")
}