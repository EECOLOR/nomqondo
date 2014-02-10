package org.qirx.nomqondo.factories

import org.specs2.mutable.Specification
import org.qirx.nomqondo.api.Failure
import org.qirx.nomqondo.api.Success
import org.qirx.nomqondo.api.Violation

object PatternSpec extends Specification {

  "Pattern" should {

    object MyCheck extends Pattern[String](
        messagePrefix = "x", 
        pattern = """\d""") {
      def apply(value: String) = value
    }

    val invalid = "invalid"

    "allow for regex pattern based checks" in {

      MyCheck.from(invalid) === Failure(MyCheck.Invalid(invalid))
      MyCheck.from("1") === Success("1")
    }

    "have the correct message in case of failure" in {
      MyCheck.from(invalid) must beLike {
        case Failure(Seq(violation: Violation)) =>
          violation.message === "x.invalid"
          violation.arguments === Seq(invalid, """\d""")
      }
    }

  }

}