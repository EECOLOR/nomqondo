package org.qirx.nomqondo.common

import org.specs2.mutable.Specification
import org.qirx.nomqondo.api.Result
import org.qirx.nomqondo.api.Violation
import org.qirx.nomqondo.api.Success
import org.qirx.nomqondo.api.Failure

object NumberSpec extends Specification {

  "Number" should {

    def test[O, V <: Violation](s: String, convert: NonEmptyString => Result[O, V]) =
      NonEmptyString.from(s).flatMap(convert)

    "Convert ints" >> {

      "with success" in {
        test("1", Number.Int.from) === Success(1)
      }

      "with failure" in {
        test("no-number", Number.Int.from) must beLike {
          case Failure(Seq(violation)) =>
            violation.message === "int.conversionFailed"
        }
      }
    }

    "Convert longs" >> {

      "with success" in {
        test("1", Number.Long.from) === Success(1L)
      }

      "with failure" in {
        test("no-number", Number.Long.from) must beLike {
          case Failure(Seq(violation)) =>
            violation.message === "long.conversionFailed"
        }
      }
    }

    "Convert double" >> {

      "with success" in {
        test("1", Number.Double.from) === Success(1D)
        test("1.1", Number.Double.from) === Success(1.1)
      }

      "with failure" in {
        test("no-number", Number.Double.from) must beLike {
          case Failure(Seq(violation)) =>
            violation.message === "double.conversionFailed"
        }
      }
    }
  }

}