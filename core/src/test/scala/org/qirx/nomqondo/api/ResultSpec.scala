package org.qirx.nomqondo.api

import org.specs2.mutable.Specification

object ResultSpec extends Specification {

  "Result" should {

    "be covariant and have success and failure types" in {

      var result: Result[Int, Violation] = null

      result = Success(0)
      result = Failure(violation)

      success
    }

    "have an isSuccess" in {
      failureAsResult.isSuccess === false
      successAsResult.isSuccess === true
    }

    "have an isFailure" in {
      failureAsResult.isFailure === true
      successAsResult.isFailure === false
    }

    "have a flatMap method" in {

      def successMapping(i: Int) = Success(i + 1)
      def failureMapping(i: Int) = Failure(Seq(violation, violation))

      Success(0).flatMap(successMapping) === Success(1)
      Success(0).flatMap(failureMapping) === Failure(Seq(violation, violation))
      failureAsResult.flatMap(successMapping) === Failure(violation)
      failureAsResult.flatMap(failureMapping) === Failure(violation)
    }

    "have a map method" in {

      def successMapping(i: Int) = i + 1

      Success(0).map(successMapping) === Success(1)
      failureAsResult.map(successMapping) === Failure(violation)
    }

    "have a factory method" in {
      Result(0) === Success(0)
    }

    "have an implicit conversion to Either" in {
      val e:Either[Seq[SimpleViolation], Int] = successAsResult
      success
    }

    "provide a factory method that accepts one failure" in {
      Failure(violation)
      success
    }
  }

  val violation = new SimpleViolation("") {}
  val failureAsResult: Result[Int, SimpleViolation] = Failure(violation)
  val successAsResult: Result[Int, SimpleViolation] = Success(0)
}