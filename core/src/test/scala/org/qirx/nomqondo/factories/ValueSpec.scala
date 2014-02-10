package org.qirx.nomqondo.factories

import org.specs2.mutable.Specification

object ValueSpec extends Specification {

  "Value" should {

    val v =
      new Value[Int] {
        val value: Int = 1
      }

    "Provide access to the value" in {
      v.value === 1
    }

    "Provide a generic toString method" in {
      implicit def toString(i:Int) = i.toString
      val s = v.toString
      s === "1"
    }

    "Provide an implicit conversion to it's value" in {
      val i:Int = v
      i === 1
    }
  }

}