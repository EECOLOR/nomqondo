package org.qirx.nomqondo.api

trait Violation {
  def message: String
  def arguments: Seq[Any]
}

abstract class SimpleViolation(val message:String, val arguments:Any *) extends Violation

// Note that you can not read the values of a case class if you use the SimpleViolation, see: https://issues.scala-lang.org/browse/SI-7436
abstract class CaseViolation(val message:String, val arguments:Seq[Any]) extends Violation
