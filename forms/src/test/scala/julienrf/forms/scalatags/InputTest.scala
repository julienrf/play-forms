package julienrf.forms.scalatags

import julienrf.forms.codecs.Codec
import julienrf.forms.codecs.Codec.{int, text}
import julienrf.forms.codecs.Constraint.greaterOrEqual
import julienrf.forms.{Mandatory, InputType, FieldData}
import org.apache.commons.lang3.StringEscapeUtils
import org.scalacheck.Prop._
import org.scalacheck.Properties

object InputTest extends Properties("Input") {

  property("derive HTML validation attributes from Rule constraints") = {

    val html5ValidationAttributes = Seq("required", "pattern", "min", "max", "step", "maxlength")
    def hasOnlyValidationAttrs(nameAndMaybeValues: (String, Option[String])*)(form: Form[_]): Boolean = {
      val hasNoOtherValidationAttr =
        html5ValidationAttributes
          .filter(n => !nameAndMaybeValues.exists { case (nn, _) => nn == n })
          .forall(n => !hasAttr(n, None)(form.empty))
      val hasAllSuppliedValidationAttrs =
        nameAndMaybeValues.forall { case (name, maybeValue) =>
          hasAttr(name, maybeValue)(form.empty)
        }
      hasAllSuppliedValidationAttrs && hasNoOtherValidationAttr
    }

    def p[A : Mandatory : InputType](nameAndMaybeValues: (String, Option[String])*)(rule: Codec[FieldData, A]): Boolean = {
      val path = "foo"
      val attrsWithoutRequired = nameAndMaybeValues.filter { case (n, _) => n != "required" }
      val optRemovesRequired = hasOnlyValidationAttrs(attrsWithoutRequired: _*)(Form.field(path, rule.?)(Control.input))
      hasOnlyValidationAttrs(nameAndMaybeValues: _*)(Form.field(path, rule)(Control.input)) && optRemovesRequired
    }

    p("required" -> None)(text) &&
    p("required" -> None)(int) &&
    p("required" -> None, "min" -> Some("42"))(int >=> greaterOrEqual(42))
  }

  property("the validation attributes derivation logic is extensible") = undecided

  property("derive the input type according to the Reads type") = {
    def p[A : Mandatory : InputType](tpe: String)(rule: Codec[FieldData, A]): Boolean =
      hasAttr("type", Some(tpe))(Form.field("foo", rule)(Control.input).empty)

    p("text")(text) &&
    p("number")(int)
  }

  property("the input type derivation logic is extensible") = undecided

  def hasAttr(name: String, maybeValue: Option[String])(html: ScalaTags.Bundle.Frag): Boolean = {
    maybeValue match {
      case Some(value) =>
        html.render.containsSlice(s"""$name="${StringEscapeUtils.escapeXml11(value)}"""")
      case None =>
        html.render.containsSlice(name)
    }
  }

}
