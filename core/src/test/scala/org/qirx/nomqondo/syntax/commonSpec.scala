package org.qirx.nomqondo.syntax

import org.specs2.mutable.Specification
import org.qirx.nomqondo.api.Violation
import org.qirx.nomqondo.common.Email
import org.qirx.nomqondo.api.Result
import org.qirx.nomqondo.common.NonEmptyString
import org.qirx.nomqondo.common.Number

object commonSpec extends Specification {

  "common" should {

    import common._

    "provide functions for common elements" in {

      def test[T](t:T) = success

      test[String => Result[NonEmptyString, NonEmptyString.Empty]](nonEmptyString)
      test[String => Result[Option[NonEmptyString], Nothing]](nonEmptyStringOpt)

      test[String => Result[Email, Email.Invalid]](email)

      test[NonEmptyString => Result[Int, Number.Int.ConversionFailed]](int)
      test[NonEmptyString => Result[Long, Number.Long.ConversionFailed]](long)
      test[NonEmptyString => Result[Double, Number.Double.ConversionFailed]](double)
    }
  }

}