package org.qirx.nomqondo.common

import org.specs2.mutable.Specification
import org.qirx.nomqondo.api.Success
import org.qirx.nomqondo.api.Failure

object NonEmptyStringSpec extends Specification {

  "NonEmptyString" should {

    "succeed for a non empty string" in {
      NonEmptyString.from(string) must beLike {
        case Success(NonEmptyString(s)) => s === string
      }
    }

    "fail for an empty string" in {
      NonEmptyString.from("") === Failure(NonEmptyString.Empty)
    }

    "provide an option constructor" in {
      NonEmptyString.asOption("") === None
      NonEmptyString.asOption(string) === Some(nonEmptyString)
    }

    "provide an implicit conversion to it's value" in {
      NonEmptyString.from(string) must beLike {
        case Success(s) => (s:String) === string
      }
    }
  }

  val string = "non-empty"
  def nonEmptyString = NonEmptyString.from(string).get
}