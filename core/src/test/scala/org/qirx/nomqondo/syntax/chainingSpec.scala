package org.qirx.nomqondo.syntax

import org.specs2.mutable.Specification
import org.qirx.nomqondo.api.Result
import org.qirx.nomqondo.api.Violation

object chainingSpec extends Specification {

  "chainging" should {

    import common._
    import chaining._

    "provide implicit methods for chaining" in {

      val number: String => Result[Int, Violation] =
        nonEmptyString chainTo int

      val numberOpt: String => Result[Option[Int], Violation] =
        nonEmptyStringOpt chainTo int

      success
    }
    "provide implicit methods for chaining with alternative syntax" in {
      val number: String => Result[Int, Violation] =
        nonEmptyString ~> int

      val numberOpt: String => Result[Option[Int], Violation] =
        nonEmptyStringOpt ~> int

      success
    }

  }

}