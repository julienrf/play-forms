package julienrf.forms.ui

import julienrf.forms.Reads
import julienrf.forms.rules.Rule
import julienrf.forms.rules.UsualRules.{text, int, min, opt}
import org.apache.commons.lang3.StringEscapeUtils
import org.scalacheck.Properties
import org.scalacheck.Prop._
import play.api.data.mapping.Path
import julienrf.forms.Generators._

object InputTest extends Properties("Input") {

  property("derive the input name attribute from the field Path") = forAll { (path: Path) =>
    val input = Input.fromReads(Reads(path, text))
    val fieldPath = path.path.mkString(".")
    hasAttr("name", Some(fieldPath))(input.tag)
  }

  property("derive HTML validation attributes from Reads constraints") = {

    val html5ValidationAttributes = Seq("required", "pattern", "min", "max", "step", "maxlength")
    def hasOnlyValidationAttrs(nameAndMaybeValues: (String, Option[String])*)(elem: scalatags.Text.Tag): Boolean = {
      val hasNoOtherValidationAttr =
        html5ValidationAttributes
          .filter(n => !nameAndMaybeValues.exists { case (nn, _) => nn == n })
          .forall(n => !hasAttr(n, None)(elem))
      val hasAllSuppliedValidationAttrs =
        nameAndMaybeValues.forall { case (name, maybeValue) =>
          hasAttr(name, maybeValue)(elem)
        }
      hasAllSuppliedValidationAttrs && hasNoOtherValidationAttr
    }

    def prove[A : Mandatory : InputType](nameAndMaybeValues: (String, Option[String])*)(rule: Rule[String, A]): Boolean = {
      val path = Path \ "foo"
      val attrsWithoutRequired = nameAndMaybeValues.filter { case (n, _) => n != "required" }
      val optRemovesRequired = hasOnlyValidationAttrs(attrsWithoutRequired: _*)(Input.fromReads(Reads(path, opt(rule))).tag)
      hasOnlyValidationAttrs(nameAndMaybeValues: _*)(Input.fromReads(Reads(path, rule)).tag) && optRemovesRequired
    }

    prove("required" -> None)(text) &&
    prove("required" -> None)(int) &&
    prove("required" -> None, "min" -> Some("42"))(int >>> min(42))
  }

  property("the validation attributes derivation logic is extensible") = undecided

  property("derive the input type according to the Reads type") = undecided

  property("the input type derivation logic is extensible") = undecided

  def hasAttr(name: String, maybeValue: Option[String])(elem: scalatags.Text.Tag): Boolean = maybeValue match {
    case Some(value) =>
      elem.toString().containsSlice(s"""$name="${StringEscapeUtils.escapeXml(value)}"""")
    case None =>
      elem.toString().containsSlice(name)
  }

}
