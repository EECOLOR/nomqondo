package org.qirx.nomqondo.factories

import org.specs2.mutable.Specification
import org.qirx.nomqondo.api.Failure
import org.qirx.nomqondo.api.Success
import org.qirx.nomqondo.api.Violation

object RangeSpec extends Specification {

  "Range" should {

    object MyCheck extends Range[Int](messagePrefix = "y", min = 1, max = 2) {
      def apply(i: Int) = i
    }

    "allow for range based checks" in {
      MyCheck.from(0) === Failure(MyCheck.BelowMin(0))
      MyCheck.from(3) === Failure(MyCheck.AboveMax(3))
      MyCheck.from(1) === Success(1)
      MyCheck.from(2) === Success(2)
    }

    "have the correct prefixes for the messages" in {
      MyCheck.from(0) must beLike {
        case Failure(Seq(violation: Violation)) =>
          violation.message === "y.belowMin"
          violation.arguments === Seq(0, 1)
      }
      MyCheck.from(3) must beLike {
        case Failure(Seq(violation: Violation)) =>
          violation.message === "y.aboveMax"
          violation.arguments === Seq(3, 2)
      }

    }
  }

}