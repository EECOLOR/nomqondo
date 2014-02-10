package org.qirx.nomqondo.common

import org.qirx.nomqondo.factories.Catching

object Number {
  object Int extends Catching(
    messagePrefix = "int",
    convert = _.toInt)

  object Long extends Catching(
    messagePrefix = "long",
    convert = _.toLong)

  object Double extends Catching(
    messagePrefix = "double",
    convert = _.toDouble)
}