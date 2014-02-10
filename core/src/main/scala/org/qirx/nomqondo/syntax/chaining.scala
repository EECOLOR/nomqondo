package org.qirx.nomqondo.syntax

import org.qirx.nomqondo.api.Result
import org.qirx.nomqondo.api.Violation
import org.qirx.nomqondo.api.Success

object chaining {

  implicit class Chainer[A, B](base: A => Result[B, Violation]) {
    def ~>[C](f: B => Result[C, Violation]): A => Result[C, Violation] =
      chainTo(f)

    def chainTo[C](f: B => Result[C, Violation]): A => Result[C, Violation] =
      base andThen (_ flatMap f)
  }

  implicit class OptionChainer[A, B](base: A => Result[Option[B], Violation]) {
    def ~>[C](f: B => Result[C, Violation]): A => Result[Option[C], Violation] =
      chainTo(f)

    def chainTo[C](f: B => Result[C, Violation]): A => Result[Option[C], Violation] =
      base andThen (_ flatMap {
        case Some(value) =>
          f(value).map(Some.apply)
        case None => Success(None)
      })
  }
}