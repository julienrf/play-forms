package julienrf.forms.ui

import julienrf.forms.Reads
import julienrf.forms.rules.{AndThen, Min, Rule}
import play.api.mvc.Call

case class Ui(input: scalatags.Text.Tag)

object Ui {
  import scalatags.Text.{attrs, tags, Modifier}
  import scalatags.Text.all._

  def fromReads[A : Mandatory : InputType](reads: Reads[A]): Ui = {
    val modifiers = Seq(
      attrs.`type` := InputType[A].tpe,
      attrs.name := reads.path.path.mkString(".") // FIXME Check that it works with all kinds of [[PathNode]]
    ) ++ validationAttrs(reads.rule)
    Ui(
      input = tags.input(modifiers: _*)
    )
  }

  def validationAttrs[A : Mandatory](rule: Rule[_, A]): Seq[Modifier] =
    if (Mandatory[A].value) validationAttrsFromRules(rule) :+ (attrs.required := "required")
    else validationAttrsFromRules(rule)

  def validationAttrsFromRules(rule: Rule[_, _]): Seq[Modifier] = {
    rule match {
      case AndThen(rule1, rule2) => validationAttrsFromRules(rule1) ++ validationAttrsFromRules(rule2)
      case Min(num) => Seq("min".attr := num.toString)
      case _ => Seq.empty
    }
  }

  def form(route: Call)(inputs: Ui*): scalatags.Text.Tag =
    tags.form(attrs.action := route.url, attrs.method := route.method)(inputs.map(_.input): _*)

}