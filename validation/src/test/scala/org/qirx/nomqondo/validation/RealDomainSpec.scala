package org.qirx.nomqondo.validation

import org.specs2.mutable.Specification
import org.qirx.nomqondo.api.Result
import org.qirx.nomqondo.api.Violation
import org.qirx.nomqondo.api.Success
import org.qirx.nomqondo.common.NonEmptyString
import data.profile._
import org.qirx.nomqondo.common.Email
import org.qirx.nomqondo.api.Failure
import org.qirx.nomqondo.api.SimpleViolation
import org.qirx.nomqondo.factories.Value
import org.qirx.nomqondo.common.Number
import org.qirx.nomqondo.syntax.common._
import org.qirx.nomqondo.syntax.chaining._

object RealDomainSpec extends Specification {

  "RealDomain" should {

    "validate" in {

      import builders._

      sealed trait Node
      case class Leaf(value: String) extends Node
      case class Branch(kv: (String, Node)*) extends Node {
        val data = Map(kv: _*)
      }
      implicit def stringToNode(string: String) = Leaf(string)

      val age = nonEmptyString ~> int ~> Age.from
      val houseNumber = HouseNumber.from _
      val postcode = nonEmptyStringOpt ~> DutchPostcode.from
      val name = asMapping(DutchName.apply _)
      val address = DutchAddress.from _
      val profile = asMapping(Profile.apply _)
      val contactInformation = asMapping(ContactInformation.apply _)

      val structure =
        mappings(
          "name" -> mappings(
            "nameFirst" -> nonEmptyString,
            "nameLast" -> nonEmptyString,
            "nameInfix" -> nonEmptyStringOpt)
            .to(name),
          "age" -> age,
          "contactInformation" ->
            mappings(
              "address" ->
                mappings(
                  "houseNumber" -> houseNumber,
                  "postcode" -> postcode,
                  "street" -> nonEmptyStringOpt,
                  "city" -> nonEmptyStringOpt)
                .to(address),
              "email" -> email)
            .to(contactInformation))
          .to(profile)

      val data =
        Branch(
          "name" -> Branch(
            "nameFirst" -> "Tim",
            "nameInfix" -> "de",
            "nameLast" -> "Wit"),
          "age" -> "35",
          "contactInformation" -> Branch(
            "address" -> Branch(
              "houseNumber" -> "12 bis",
              "postcode" -> "3527 BE",
              "street" -> "Salamistraat",
              "city" -> "Achterweg"),
            "email" -> "my.email@my.domain.nl"))

      class NodeDataProvider(base: Branch) extends DataProviderTree[String, String] {
        def get(key: String): Option[Either[String, DataProviderTree[String, String]]] =
          base.data.get(key).map {
            case branch: Branch => Right(new NodeDataProvider(branch))
            case Leaf(value) => Left(value)
          }
      }

      val dataProvider = new NodeDataProvider(data)

      val result = structure.apply(dataProvider)

      implicit class ValueEquals[A](v: => Value[A]) {
        def ===[B](b: => B) = v.value === b
      }

      result must beLike {
        case Success(profile) =>
          profile.name must beLike {
            case DutchName(nameFirst, nameLast, Some(nameInfix)) =>
              nameFirst === "Tim"
              nameInfix === "de"
              nameLast === "Wit"
          }
          profile.age === 35
          profile.contactInformation must beLike {
            case ContactInformation(address, email) =>
              address must beLike {
                case DutchAddress(
                  HouseNumber(number, Some(suffix)),
                  Some(DutchPostcode(postcode)),
                  Some(street),
                  Some(city)) =>

                  number === 12
                  suffix === "bis"
                  postcode === "3527 BE"
                  street === "Salamistraat"
                  city === "Achterweg"
              }
              email === "my.email@my.domain.nl"
          }
      }

      todo

    }
  }
}