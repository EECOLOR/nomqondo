package org.qirx.nomqondo.factories

import org.specs2.mutable.Specification
import org.qirx.nomqondo.common.NonEmptyString
import org.qirx.nomqondo.api.Success
import org.qirx.nomqondo.api.Failure

object CatchingSpec extends Specification {

  "Catching" should {

    object Test extends Catching("test", _.toInt)

    def test(s: String) =
      NonEmptyString.from(s).flatMap(Test.from)

    "Succeed for a conversion that does not throw an exception" in {
      test("1") === Success(1)
    }

    "Fail for a convertsion that throws an exception" in {
      test("no-number") must beLike {
        case Failure(Seq(violation)) =>
          violation.message === "test.conversionFailed"
          violation.arguments === Seq("no-number")
      }
    }
  }

}