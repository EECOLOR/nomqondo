package org.qirx.nomqondo.factories

class ToString[T <% String] {
  def apply(t:T):String = t
}

object ToString {

  implicit def toString[T <% String]:ToString[T] = new ToString[T]
}