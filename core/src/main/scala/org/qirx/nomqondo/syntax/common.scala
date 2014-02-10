package org.qirx.nomqondo.syntax

import org.qirx.nomqondo.common.NonEmptyString
import org.qirx.nomqondo.common.Email
import org.qirx.nomqondo.api.Result
import org.qirx.nomqondo.common.Number

object common {

  val nonEmptyString = NonEmptyString.from _
  val nonEmptyStringOpt = NonEmptyString.asOption _ andThen (Result apply _)

  val email = Email.from _

  val int = Number.Int.from _
  val long = Number.Long.from _
  val double = Number.Double.from _
}