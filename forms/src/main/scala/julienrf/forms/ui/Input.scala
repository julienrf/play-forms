package julienrf.forms.ui

import julienrf.forms.Reads
import julienrf.forms.rules._
import play.api.mvc.Call

case class Input(tag: scalatags.Text.Tag)

object Input {
  import scalatags.Text.{attrs, tags, Modifier}
  import scalatags.Text.all._

  def fromReads[A : Mandatory : InputType](reads: Reads[A]): Input = {
    val modifiers = Seq(
      attrs.`type` := InputType[A].tpe,
      attrs.name := reads.path.path.mkString(".") // FIXME Check that it works with all kinds of [[PathNode]]
    ) ++ validationAttrs(reads.rule)
    Input(
      tag = tags.input(modifiers: _*)
    )
  }

  def validationAttrs[A : Mandatory](rule: Rule[_, A]): Seq[Modifier] =
    if (Mandatory[A].value) validationAttrsFromRules(rule) :+ (attrs.required := "required")
    else validationAttrsFromRules(rule)

  def validationAttrsFromRules(rule: Rule[_, _]): Seq[Modifier] =
    rule match {
      case AndThen(lhs, rhs) => validationAttrsFromRules(lhs) ++ validationAttrsFromRules(rhs)
      case And(lhs, rhs) => validationAttrsFromRules(lhs) ++ validationAttrsFromRules(rhs)
      case Min(num) => Seq("min".attr := num.toString)
      case Opt(rule) => validationAttrsFromRules(rule)
      case Head | Id() | ToInt | Or(_, _) => Seq.empty
    }

  def form(route: Call)(inputs: Input*): scalatags.Text.Tag =
    tags.form(attrs.action := route.url, attrs.method := route.method)(inputs.map(_.tag): _*)

}