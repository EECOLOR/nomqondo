Nomqondo
========

A Zulu word that (according to Google translate) means sensible and meaningful.

This is a set of small libraries that help with creating more meaningful domain
objects.

The main idea is that the construction of an object might fail because some of
the business rules were violated.

A second important idea is that simple types like `String` and `Int` give little
meaning. A `name` in most cases should be a non-empty `String`. And `age` is most
likely an integer value between `x` and `y`, depending on your domain.

The following libraries are present:

- **core** - The basic notion of a result and some commonly used factories
- **play** - Some utilities that make interoperability with Play forms easy
- **validation** - An experiment that allows you to construct structures that can be validated in a type-safe manner

Installation
============

TODO

Core
====

**Elimination of basic types**

A very basic example where we introduced the notion of more meaningful types is this:

```scala
case class DutchName(
  nameFirst: NonEmptyString,
  nameLast: NonEmptyString,
  nameInfix: Option[NonEmptyString])
```

**A result instead of of a value**

Creating one of these *special* types works like this:

```scala
val result = NonEmptyString.from(s)
result match {
  case Success(value) => ...
  case Failure(Seq(NonEmptyString.Empty)) => ...
}
```

**A more elaborate example**

In this example our domain dictates that an address is valid when a postcode is
defined or when the combination of street and city are defined.

```scala
case class DutchAddress private (
  houseNumber: HouseNumber,
  postcode: Option[DutchPostcode],
  street: Option[NonEmptyString],
  city: Option[NonEmptyString])

object DutchAddress {

  type Invalid = Invalid.type

  def from(
    houseNumber: HouseNumber,
    postcode: Option[DutchPostcode],
    street: Option[NonEmptyString],
    city: Option[NonEmptyString]): Result[DutchAddress, Invalid] = {

    if (postcode.isDefined | (street.isDefined & city.isDefined))
      Success(DutchAddress(houseNumber, postcode, street, city))
    else Failure(Invalid)
  }

  case object Invalid extends SimpleViolation("dutchAddress.invalid")

}
```

**Mapping results**

Results can be mapped and flat mapped:

```scala
val result:Result[NonEmptyString, NonEmptyString.Empty] = ???
val nameResult =
  result.map { nonEmptyString =>
    Name(nonEmptyString)
  }

val intResult = result.flatMap(Number.Int.from)
```

**Syntax improvements**

Common elements have utilities that allow them to be used more easily (see the
`syntax.common` and `syntax.chaining` pacakages.

```scala
import org.qirx.nomqondo.syntax.common._
import org.qirx.nomqondo.syntax.chaining._

val age = nonEmptyString ~> int ~> Age.from
// or alternatively
val age = nonEmptyString chainTo int chainTo Age.from
```

**Tests**

The test directory contains more examples of usage

Play
====

This library allows you to use `String` -> `String => Result[?, ?]` pairs as
Play mappings:

```scala
import org.qirx.nomqondo.syntax.common._
import org.qirx.nomqondo.syntax.chaining._

Form(mapping(
  "email" -> email, //Shorthand for Email.from _
  "name" -> nonEmptyString ~> int))
```

Validation
==========

This is more of an experiment that I used to learn more about type-level
programming. Check out the test directory if you find it interesting.
