package data.profile

import org.qirx.nomqondo.common.NonEmptyString
import org.qirx.nomqondo.common.Email

case class ContactInformation(
  address: DutchAddress,
  email: Email)
