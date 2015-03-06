package julienrf.forms.presenters

import julienrf.forms.{Form, FormData}
import julienrf.forms.rules.Rule
import julienrf.forms.rules.Rule.{text, int, min}
import org.apache.commons.lang3.StringEscapeUtils
import org.scalacheck.Properties
import org.scalacheck.Prop._

object FieldTest extends Properties("Field") {

  property("derive HTML validation attributes from Rule constraints") = {

    val html5ValidationAttributes = Seq("required", "pattern", "min", "max", "step", "maxlength")
    def hasOnlyValidationAttrs(nameAndMaybeValues: (String, Option[String])*)(form: Form[_]): Boolean = {
      val hasNoOtherValidationAttr =
        html5ValidationAttributes
          .filter(n => !nameAndMaybeValues.exists { case (nn, _) => nn == n })
          .forall(n => !hasAttr(n, None)(form.empty.html))
      val hasAllSuppliedValidationAttrs =
        nameAndMaybeValues.forall { case (name, maybeValue) =>
          hasAttr(name, maybeValue)(form.empty.html)
        }
      hasAllSuppliedValidationAttrs && hasNoOtherValidationAttr
    }

    def p[A : Mandatory : InputType](nameAndMaybeValues: (String, Option[String])*)(rule: Rule[(FormData, String), A]): Boolean = {
      val path = "foo"
      val attrsWithoutRequired = nameAndMaybeValues.filter { case (n, _) => n != "required" }
      val optRemovesRequired = hasOnlyValidationAttrs(attrsWithoutRequired: _*)(Form.field(path, rule.?)(Input.input))
      hasOnlyValidationAttrs(nameAndMaybeValues: _*)(Form.field(path, rule)(Input.input)) && optRemovesRequired
    }

    p("required" -> None)(text) &&
    p("required" -> None)(int) &&
    p("required" -> None, "min" -> Some("42"))(int >=> min(42))
  }

  property("the validation attributes derivation logic is extensible") = undecided

  property("derive the input type according to the Reads type") = {
    def p[A : Mandatory : InputType](tpe: String)(rule: Rule[(FormData, String), A]): Boolean =
      hasAttr("type", Some(tpe))(Form.field("foo", rule)(Input.input).empty.html)

    p("text")(text) &&
    p("number")(int)
  }

  property("the input type derivation logic is extensible") = undecided

  def hasAttr(name: String, maybeValue: Option[String])(html: Seq[scalatags.Text.Modifier]): Boolean = {
    val elem = scalatags.Text.tags.div(html: _*)
    maybeValue match {
      case Some(value) =>
        elem.toString().containsSlice(s"""$name="${StringEscapeUtils.escapeXml(value)}"""")
      case None =>
        elem.toString().containsSlice(name)
    }
  }

}
