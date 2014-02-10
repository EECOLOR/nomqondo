package org.qirx.nomqondo.api

sealed abstract class Result[+O, +V <: Violation] {
  def isSuccess: Boolean
  def isFailure: Boolean

  def flatMap[O2, V2 >: V <: Violation](f: => O => Result[O2, V2]): Result[O2, V2] =
    this match {
      case Success(value) => f(value)
      case Failure(violations) => Failure(violations)
    }

  def map[O2](f: O => O2): Result[O2, V] =
    flatMap(x => Success(f(x)))

  def get:O
}

object Result {

  def apply[O](s: O): Result[O, Nothing] =
    Success(s)

  import scala.language.implicitConversions

  implicit def toEither[O, V <: Violation](r: Result[O, V]): Either[Seq[V], O] =
    r match {
      case Success(value) => Right(value)
      case Failure(violations) => Left(violations)
    }
}

case class Failure[V <: Violation](violations: Seq[V]) extends Result[Nothing, V] {
  val isSuccess = false
  val isFailure = true

  def get = throw new NoSuchElementException("Failure.get")
}

object Failure {
  def apply[V <: Violation](t: V): Failure[V] = Failure(Seq(t))
}

case class Success[O](value: O) extends Result[O, Nothing] {
  val isSuccess = true
  val isFailure = false

  def get = value
}