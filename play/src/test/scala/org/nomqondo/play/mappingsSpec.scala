package org.nomqondo.play

import org.specs2.mutable.Specification
import org.qirx.nomqondo.syntax.common._
import org.qirx.nomqondo.syntax.chaining._
import play.api.data.Forms._
import play.api.data.Mapping
import org.qirx.nomqondo.api.Result
import org.qirx.nomqondo.api.Violation
import org.qirx.nomqondo.common.NonEmptyString
import play.api.data.FormError

object mappingsSpec extends Specification {

  "mappings" should {

    import mappings._

    "provide implicit conversions to a mapping" in {

      val m = tuple(
        "test1" -> nonEmptyString ~> int,
        "test2" -> nonEmptyString)

      m.bind(Map("test1" -> "1", "test2" -> "2")) ===
        Right(1 -> NonEmptyString.from("2").get)

      m.bind(Map("test1" -> "no-number", "test2" -> "")) ===
        Left(Seq(FormError("test1", "int.conversionFailed", Seq("no-number")),
          FormError("test2", "nonEmptyString.empty", Seq.empty)))
    }
  }

}