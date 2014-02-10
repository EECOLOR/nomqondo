package org.qirx.nomqondo.factories

trait Value[T] {
  def value:T

  def toString(implicit s:ToString[T]):String = s(value)
}

object Value {

  import scala.language.implicitConversions
  implicit def toValue[T](value:Value[T]):T = value.value
}
