package org.qirx.nomqondo.common

import org.qirx.nomqondo.factories.Pattern
import org.qirx.nomqondo.factories.Value

case class Email private(value:String) extends Value[String]

object Email extends Pattern[Email](
    messagePrefix = "email",
    pattern = """^(?!\.)("([^"\r\\]|\\["\r\\])*"|([-a-zA-Z0-9!#$%&'*+/=?^_`{|}~]|(?<!\.)\.)*)(?<!\.)@[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]\.[a-zA-Z][a-zA-Z\.]*[a-zA-Z]$""")