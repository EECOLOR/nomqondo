package org.qirx.nomqondo.api

import org.specs2.mutable.Specification

object ViolationSpec extends Specification {

  "Violation" should {

    "have a message and arbitrary arguments" in {

      new Violation {
        override def message: String = ""
        override def arguments: Seq[Any] = Seq.empty
      }

      success
    }

    "have an abstract sub type that can be used for easy construction" in {

      object CustomViolation extends SimpleViolation("message", 1, 2)

      CustomViolation.message === "message"
      CustomViolation.arguments === Seq(1, 2)
    }
  }

}