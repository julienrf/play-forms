package julienrf.forms.presenters

import ScalaTags.bundle._
import julienrf.forms._
import julienrf.forms.rules._

object Input {

  def input[A : Mandatory : InputType]: Presenter[A] = input[A]()

  def input[A : Mandatory : InputType](additionalAttrs: (String, String)*): Presenter[A] = new Presenter[A] {
    def render(field: Field[A]) =
      FormUi(Seq(
        <.input(
          %.`type` := InputType[A].tpe,
          %.name := field.name,
          %.value := field.value.getOrElse(""),
          (Input.validationAttrs(field.rule) ++ additionalAttrs).map { case (n, v) => n.attr := v}.to[Seq]
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
      <.option(%.value := value, if (fieldValue contains value) Seq("selected".attr := "selected") else Seq.empty[Modifier])(label)
    }

  def select[A : Mandatory](opts: Option[String] => Seq[scalatags.Text.Tag]): Presenter[A] = new Presenter[A] {
    def render(field: Field[A]) =
      FormUi(Seq(
        <.select(%.name := field.name, if (Mandatory[A].value) Seq(%.required := "required") else Seq.empty[Modifier])(
          opts(field.value)
        )
      ))
  }

  def enumOptions[A](values: Set[A], keys: A => String, labels: A => String): Seq[(String, String)] =
    ("" -> "") +: (values.to[Seq] map (a => keys(a) -> labels(a)))

}