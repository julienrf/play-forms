package julienrf.forms.ui

import julienrf.forms.Reads
import julienrf.forms.rules.{AndThen, Min, Rule}
import play.api.mvc.Call

case class Ui(input: scalatags.Text.Tag)

object Ui {
  import scalatags.Text.{attrs, tags, Modifier}
  import scalatags.Text.all._

  def fromReads[O : InputAttrs](reads: Reads[O]): Ui = {
    val modifiers = Seq(
      attrs.`type` := InputAttrs[O].InputType.tpe,
      attrs.name := reads.path.path.mkString(".") // FIXME Check that it works with all kinds of [[PathNode]]
    ) ++ validationAttributes(reads.rule) ++ (if (InputAttrs[O].Mandatory.value) Seq(attrs.required := "required") else Nil) // TODO Handle required attr in validationAttributes
    Ui(
      input = tags.input(modifiers: _*)
    )
  }

  def validationAttributes[A, B](rule: Rule[A, B]): Seq[Modifier] = {
    rule match {
      case AndThen(rule1, rule2) => validationAttributes(rule1) ++ validationAttributes(rule2)
      case Min(num) => Seq("min".attr := num.toString)
      case _ => Seq.empty
    }
  }

  def form(route: Call)(inputs: Ui*): scalatags.Text.Tag =
    tags.form(attrs.action := route.url, attrs.method := route.method)(inputs.map(_.input): _*)

}