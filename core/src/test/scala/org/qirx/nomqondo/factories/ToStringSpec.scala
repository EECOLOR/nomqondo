package org.qirx.nomqondo.factories

import org.specs2.mutable.Specification

object ToStringSpec extends Specification {

  "ToString" should {

    "supply an instance for any view of string" in {

      implicit def intToString(i: Int) = i.toString

      implicitly[ToString[String]].apply("test") === "test"
      implicitly[ToString[Int]].apply(1) === "1"

      new ToString[String].apply("test") === "test"
      new ToString[Int].apply(1) === "1"
    }

  }

}