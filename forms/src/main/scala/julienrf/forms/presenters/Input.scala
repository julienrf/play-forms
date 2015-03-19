package julienrf.forms.presenters

import julienrf.forms._
import julienrf.forms.rules._

object Input {

  import scalatags.Text.{attrs, tags}
  import scalatags.Text.all._

  def input[A : Mandatory : InputType]: Presenter[A] = input[A]()

  def input[A : Mandatory : InputType](additionalAttrs: (String, String)*): Presenter[A] = new Presenter[A] {
    def render(name: String, rule: Rule[_, A], value: Option[String], errors: Seq[Throwable]) =
      FormUi(Seq(
        tags.input(
          attrs.`type` := InputType[A].tpe,
          attrs.name := name,
          attrs.value := value.getOrElse(""),
          (Input.validationAttrs(rule) ++ additionalAttrs).map { case (n, v) => n.attr := v}.to[Seq]
        )
      ))
  }

  def validationAttrs[A: Mandatory](rule: Rule[_, A]): Map[String, String] =
    if (Mandatory[A].value) validationAttrsFromRules(rule) + ("required" -> "required")
    else validationAttrsFromRules(rule)

  // TODO Make this extensible
  def validationAttrsFromRules(rule: Rule[_, _]): Map[String, String] =
    rule match {
      case AndThen(lhs, rhs) => validationAttrsFromRules(lhs) ++ validationAttrsFromRules(rhs)
      //      case And(lhs, rhs) => validationAttrsFromRules(lhs) ++ validationAttrsFromRules(rhs)
      case Min(num) => Map("min" -> num.toString)
      case Opt(rule) => validationAttrsFromRules(rule)
      case Head | /*Id() |*/ ToInt | Or(_, _) | OneOf(_) => Map.empty
    }

  def options(data: Seq[(String, String)])(fieldValue: Option[String]): Seq[scalatags.Text.Tag] =
    for ((value, label) <- data) yield {
      tags.option(attrs.value := value, if (fieldValue contains value) Seq("selected".attr := "selected") else Seq.empty[Modifier])(label)
    }

  def select[A : Mandatory](opts: Option[String] => Seq[scalatags.Text.Tag]): Presenter[A] = new Presenter[A] {
    def render(name: String, rule: Rule[_, A], value: Option[String], errors: Seq[Throwable]) =
      FormUi(Seq(
        tags.select(attrs.name := name, if (Mandatory[A].value) Seq(attrs.required := "required") else Seq.empty[Modifier])(
          opts(value)
        )
      ))
  }

  def enumOptions[A](values: Set[A], keys: A => String, labels: A => String): Seq[(String, String)] =
    ("" -> "") +: (values.to[Seq] map (a => keys(a) -> labels(a)))

}