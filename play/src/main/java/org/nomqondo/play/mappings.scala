package org.nomqondo.play

import org.qirx.nomqondo.api.Result
import play.api.data.Mapping
import org.qirx.nomqondo.api.Violation
import play.api.data.validation.Constraint
import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.data.format.Formats
import org.qirx.nomqondo.api.Failure
import org.qirx.nomqondo.api.Success
import play.api.data.FieldMapping

object mappings {

  import scala.language.implicitConversions

  implicit def toMapping[O](m: (String, String => Result[O, Violation])): (String, Mapping[O]) = {
    val (key, value) = m

    key -> new FieldMapping[O]()(resultFormatter(value))
  }

  def resultFormatter[O](result: String => Result[O, Violation]): Formatter[O] =
    new Formatter[O] {

      def bind(key: String, data: Map[String, String]): Either[Seq[FormError], O] =
        Formats.stringFormat.bind(key, data).right.flatMap { s =>
          result(s).left.map(_.map(violationToFormError(key)))
        }

      def unbind(key: String, value: O): Map[String, String] =
        ???
    }

  private def violationToFormError(key: String)(v: Violation): FormError =
    FormError(key, v.message, v.arguments)
}