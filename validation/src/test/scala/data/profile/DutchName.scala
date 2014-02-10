package data.profile

import org.qirx.nomqondo.common.NonEmptyString
import org.qirx.nomqondo.factories.Value

case class DutchName(
  nameFirst: NonEmptyString,
  nameLast: NonEmptyString,
  nameInfix: Option[NonEmptyString]) {

  lazy val fullName =
    Seq(Some(nameFirst), nameInfix, Some(nameLast)).flatten mkString " "
}
