package data.profile

import org.qirx.nomqondo.common.NonEmptyString
import org.qirx.nomqondo.api.Result
import org.qirx.nomqondo.api.SimpleViolation
import org.qirx.nomqondo.api.Failure
import org.qirx.nomqondo.api.Success

case class DutchAddress private (
  houseNumber: HouseNumber,
  postcode: Option[DutchPostcode],
  street: Option[NonEmptyString],
  city: Option[NonEmptyString])

object DutchAddress {

  type Invalid = Invalid.type

  def from(
    houseNumber: HouseNumber,
    postcode: Option[DutchPostcode],
    street: Option[NonEmptyString],
    city: Option[NonEmptyString]): Result[DutchAddress, Invalid] = {

    if (postcode.isDefined | (street.isDefined & city.isDefined))
      Success(DutchAddress(houseNumber, postcode, street, city))
    else Failure(Invalid)
  }

  case object Invalid extends SimpleViolation("dutchAddress.invalid")

}