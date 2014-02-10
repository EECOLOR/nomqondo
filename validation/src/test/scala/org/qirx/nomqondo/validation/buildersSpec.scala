package org.qirx.nomqondo.validation

import org.specs2.mutable.Specification
import org.qirx.nomqondo.api.Result
import org.qirx.nomqondo.api.Violation
import org.qirx.nomqondo.validation.mappings.TreeMapping
import org.qirx.nomqondo.validation.mappings.KeyMapping
import org.qirx.nomqondo.validation.mappings.Mapping
import org.qirx.nomqondo.validation.mappings.TreeMapping
import org.qirx.nomqondo.validation.mappings.ValueMapping
import shapeless.ops.function.FnToProduct
import shapeless.::
import shapeless.HNil
import shapeless.HList
import org.qirx.nomqondo.api.SimpleViolation
import org.qirx.nomqondo.api.Success
import org.qirx.nomqondo.api.Failure
import org.qirx.nomqondo.validation.mappings.KeyValueMapping
import org.qirx.nomqondo.validation.mappings.KeyTreeMapping
import org.qirx.nomqondo.validation.mappings.ObjectMapping

class buildersSpec extends Specification {

  import builders._

  "builders" should {

    "provide an easy way to create a mapping" >> {

      val boolFunction = { s: Boolean => (???): Result[Int, Violation] }

      def mapping0 = mapping(boolFunction)

      "for a function that returns a result" in {
        mapping0 must beAnInstanceOf[Mapping[_, _, _]]
      }

      def mapping1 = mapping("key - function" -> boolFunction)

      "for a key - function pair" in {

        mapping1 must beAnInstanceOf[KeyValueMapping[_, _, _]]
      }

      "for a key - mapping pair" in {

        val mapping2 = mapping("key - mapping" -> mapping0)
        mapping2 must beAnInstanceOf[KeyValueMapping[_, _, _]]
      }

      "for a key - mapping pair and a key - function pair" in {

        val mappings0 = mappings(
          "key - function" -> boolFunction,
          "key - mapping" -> mapping0)

        mappings0 must beAnInstanceOf[TreeMapping[_, _, _, _]]
      }

      def mappings1 = mappings(
        "key - function" -> boolFunction,
        "key - mapping" -> mapping0,
        mapping1)

      "for a key - mapping pair, a key - function pair and a key mapping" in {

        mappings1 must beAnInstanceOf[TreeMapping[_, _, _, _]]
      }

      def mapping3 = mapping("key - mappings" -> mappings1)

      "for a key - mappings pair" in {

        mapping3 must beAnInstanceOf[KeyTreeMapping[_, _, _]]
      }

      "for a key - function pair and a key - mappings pair" in {

        val mappings2 = mappings(
          "key - function" -> boolFunction,
          "key - mappings" -> mappings1)
        mappings2 must beAnInstanceOf[TreeMapping[_, _, _, _]]
      }

      "for all types of mappings (including nested mappings)" in {

        val mappings3 = mappings(
          "key - function" -> boolFunction,
          "key - mappings" -> mappings1,
          "key - mapping" -> mapping0,
          mapping1,
          mapping3)
        mappings3 must beAnInstanceOf[TreeMapping[_, _, _, _]]
      }

      def multiArgumentFunction = { (s: String, b: Int) => (???): Result[Int, Violation] }

      "for multiArgument functions" in {
        val mapping4 = mapping(multiArgumentFunction)
        mapping4 must beAnInstanceOf[ValueMapping[_, _, _]]
      }

      val intFunction = { s: Boolean => (???): Result[Int, Violation] }
      val stringFunction = { s: Boolean => (???): Result[String, Violation] }

      "for mapping mappings to a multi argument function" in {

        val mapping5 = mappings(
          "key1" -> stringFunction,
          "key2" -> intFunction).to(multiArgumentFunction)

        mapping5 must beAnInstanceOf[ObjectMapping[_, _, _, _, _]]
      }

      "for mapping functions with different return types" in {

        val mapping6 = mappings(
          "key1" -> intFunction,
          "key2" -> stringFunction,
          "key3" -> { s: Boolean => (???): Result[Boolean, Violation] })

        mapping6 must beAnInstanceOf[TreeMapping[_, _, _, _]]
      }
    }

    "help with other (problematic) use cases" >> {

      val functionSuccess = { s: String => Success(0) }

      "mapping success" in {
        val m: ValueMapping[String, Int, Nothing] = mapping(functionSuccess)
        success
      }

      object NotAnInt extends SimpleViolation("")
      val functionFailure = { s: String => Failure(NotAnInt) }

      "mapping failure" in {
        val m: ValueMapping[String, Nothing, NotAnInt.type] = mapping(functionFailure)
        success
      }

      "mapping key - success" in {
        val m: KeyValueMapping[String, String, Int] = mapping("key" -> functionSuccess)
        success
      }

      /*
      "mapping key - failure" in {
        val mapping3 = mapping("key" -> functionFailure)
        success.pendingUntilFixed("Resolution has problems with Nothing. It might be this bug: https://issues.scala-lang.org/browse/SI-6472. It's not a real problem as it makes no sense to have a failure in the static code (no result output type).")
      }
      */

      "mappings with an option as result (after the second parameter)" in {
        val mapping4:TreeMapping[String, String, _, Result[Int, Violation] :: Result[Int, Violation] :: Result[Option[String], Violation] :: HNil] =
          mappings(
          "key" -> functionSuccess,
          "key" -> functionSuccess,
          "key" -> { s: String => Result(Option("")) })
        success
      }
    }
  }
}