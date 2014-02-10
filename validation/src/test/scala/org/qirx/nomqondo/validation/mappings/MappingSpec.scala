package org.qirx.nomqondo.validation.mappings

import org.specs2.mutable.Specification
import org.qirx.nomqondo.api.Success
import org.qirx.nomqondo.api.SimpleViolation
import scala.util.Try
import org.qirx.nomqondo.api.Failure
import org.qirx.nomqondo.validation.DataProviderTree
import shapeless.HList
import shapeless.HNil
import shapeless.::
import org.qirx.nomqondo.api.Violation
import org.qirx.nomqondo.api.Result

class MappingSpec extends Specification {

  "A value mapping" should {
    "wrap an function from input type to result" in {
      intValueMapping.apply("test") === Failure(NotAnInt)
      intValueMapping.apply("1") === Success(1)
    }
  }

  "A key mapping" should {

    val dataProvider = simpleDataProvider(Map("test1" -> "test", "test2" -> "1"))
    val test1KeyMapping = intValueKeyMapping("test1")

    "call the underlying mapping using the correct value from the data provider" in {

      test1KeyMapping.apply(dataProvider) === Failure(KeyViolation("test1", Seq(NotAnInt)))
      intValueKeyMapping("test2").apply(dataProvider) === Success(1)
    }

    "not execute the mapping expression in case of failure" in {

      new KeyValueMapping("test", ???).apply(dataProvider) ===
        Failure(KeyViolation("test", ExpectedKey))

      new KeyTreeMapping("test", ???).apply(dataProvider) ===
        Failure(KeyViolation("test", ExpectedKey))
    }

    "be able to handle mappings that require a data provider at a certain key" in {
      val nestedDataProvider = new DataProviderTree[String, String] {
        val data = Map("nested" -> dataProvider)
        def get(key: String) = data.get(key).map(Right.apply)
      }

      nestedIntValueKeyMapping("nested", "test1").apply(nestedDataProvider) ===
        Failure(KeyViolation("nested", KeyViolation("test1", NotAnInt)))

      nestedIntValueKeyMapping("nested", "test2").apply(nestedDataProvider) ===
        Success(1)
    }

    "fail for non-existing keys" in {
      val emptyDataProvider = new DataProviderTree[String, String] {
        def get(key: String) = None
      }

      test1KeyMapping.apply(emptyDataProvider) ===
        Failure(KeyViolation("test1", ExpectedKey))
    }

    "fail when expecting a value instead of a tree" in {
      val nestedDataProvider = new DataProviderTree[String, String] {
        val data = Map("test1" -> dataProvider)
        def get(key: String) = data.get(key).map(Right.apply)
      }

      test1KeyMapping.apply(nestedDataProvider) === Failure(KeyViolation("test1", ExpectedValue))
    }

    "fail when expecting a tree instead of a value" in {

      nestedIntValueKeyMapping("test1", "will not reach this key").apply(dataProvider) ===
        Failure(KeyViolation("test1", ExpectedDataProviderTree))
    }
  }

  "Tree mappings" should {
    val dataProvider = simpleDataProvider(Map("test1" -> "test", "test2" -> "1"))
    val treeMapping = new TreeMapping(
      intValueKeyMapping("test1") :: stringValueKeyMapping("test2") :: HNil)

    "Correctly turn a list of mappings into a list of results" in {

      treeMapping(dataProvider) === Success(
        Failure(KeyViolation("test1", Seq(NotAnInt))) ::
          Success("1") :: HNil)
    }

    "work with mixed key mapping types" in {

      val nestedTreeMapping = new TreeMapping(
        new KeyTreeMapping("test1", treeMapping) ::
          intValueKeyMapping("test2") ::
          HNil)

      nestedTreeMapping(dataProvider) === Success(
        Failure(KeyViolation("test1", ExpectedDataProviderTree)) ::
          Success(1) :: HNil)
    }

    "work with Nothing" in {
      new TreeMapping(
        intValueKeyMapping("test1") :: stringValueKeyMapping("test2") ::
        new KeyValueMapping("test3", new ValueMapping({s:String => (???):Result[Boolean, Nothing]})) ::
        HNil)
      success
    }
  }

  "Object mappings" should {

    val treeMapping = new TreeMapping(
      stringValueKeyMapping("test1") :: intValueKeyMapping("test2") :: HNil)

    "Correctly turn a list of mappings into an object" in {

      val objectMapping =
        new ObjectMapping(
          treeMapping,
          new ValueMapping({ i: (String :: Int :: HNil) => Success(i) }))

      val dataProvider = simpleDataProvider(Map("test1" -> "test", "test2" -> "1"))

      objectMapping(dataProvider) === Success(
        "test" :: 1 :: HNil)
    }

    "list the failures" in {
      val objectMapping =
        new ObjectMapping(treeMapping, ???)

      val dataProvider = simpleDataProvider(Map("test1" -> "test", "test2" -> "test"))

      objectMapping(dataProvider) === Failure(KeyViolation("test2", Seq(NotAnInt)))
    }

    "show failures in the correct order" in {
      val treeMapping = new TreeMapping(
        intValueKeyMapping("test1") :: intValueKeyMapping("test2") :: HNil)

      val objectMapping =
        new ObjectMapping(treeMapping, ???)

      val dataProvider = simpleDataProvider(Map("test1" -> "test", "test2" -> "test"))

      objectMapping(dataProvider) === Failure(Seq(KeyViolation("test1", Seq(NotAnInt)), KeyViolation("test2", Seq(NotAnInt))))
    }
  }

  "Key violations" should {
    "provide the key and violations as arguments" in {
      val key = "test1"
      val violations = Seq(NotAnInt)
      KeyViolation(key, violations).arguments === Seq(key, violations)
    }
  }

  case object NotAnInt extends SimpleViolation("notAnInt")

  def intValueKeyMapping(key: String) =
    new KeyValueMapping(key, intValueMapping)

  def stringValueKeyMapping(key: String) =
    new KeyValueMapping(key, new ValueMapping({ s: String => Success(s) }))

  def nestedIntValueKeyMapping(key1: String, key2: String) =
    new KeyTreeMapping(key1, intValueKeyMapping(key2))

  val intValueMapping = new ValueMapping({ s: String =>
    Try(Success(s.toInt))
      .recover { case t: Throwable => Failure(NotAnInt) }
      .get
  })

  def simpleDataProvider(data: Map[String, String]) =
    new DataProviderTree[String, String] {
      def get(key: String) = data.get(key).map(Left.apply)
    }
}