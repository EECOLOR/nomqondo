package org.qirx.nomqondo.validation.mappings

import org.qirx.nomqondo.api.Violation
import org.qirx.nomqondo.api.SimpleViolation

case class KeyViolation[K, V <: Violation](key:K, violations:Seq[V]) extends
  SimpleViolation("key.violation", key, violations)

object KeyViolation {
  def apply[K, V <: Violation](key:K, violation:V):KeyViolation[K, V] =
    KeyViolation(key, Seq(violation))
}

case object ExpectedKey extends SimpleViolation("key.expected")

case object ExpectedValue extends SimpleViolation("key.expectedValue")

case object ExpectedDataProviderTree extends SimpleViolation("key.expectedDataProviderTree")
