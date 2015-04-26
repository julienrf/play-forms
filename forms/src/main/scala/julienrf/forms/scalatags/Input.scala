package julienrf.forms.scalatags

import julienrf.forms._
import ScalaTags.Bundle._

object Input extends julienrf.forms.presenters.Input[Frag] {

  def input[A : Mandatory : InputType](additionalAttrs: (String, String)*): Presenter[A, Frag] = new Presenter[A, Frag] {
    def render(field: Field[A]): Frag =
      <.input(
        %.`type` := InputType[A].tpe,
        %.name := field.key,
        %.value := field.value.headOption.getOrElse(""),
        (validationAttrs(field.codec) ++ additionalAttrs).map { case (n, v) => n.attr := v }.to[Seq]
      )
  }

  def options(data: Seq[(String, String)])(fieldValue: Seq[String]): Frag =
    for ((value, label) <- data) yield {
      <.option(%.value := value, if (fieldValue contains value) Seq("selected".attr := "selected") else Seq.empty[Modifier])(label)
    }

  def select[A : Mandatory : Multiple](opts: Seq[String] => Frag): Presenter[A, Frag] = new Presenter[A, Frag] {
    def render(field: Field[A]): Frag =
      <.select(
        %.name := field.key,
        if (Mandatory[A].value) Seq(%.required := "required") else Seq.empty[Modifier],
        if (Multiple[A].value) Seq("multiple".attr := "multiple") else Seq.empty[Modifier]
      )(
        opts(field.value)
      )
  }

  def checkbox(additionalAttrs: (String, String)*): Presenter[Boolean, Frag] = new Presenter[Boolean, Frag] {
    def render(field: Field[Boolean]): Frag =
      <.input(
        %.`type` := "checkbox",
        %.name := field.key,
        %.value := "true",
        additionalAttrs.map { case (n, v) => n.attr := v}.to[Seq],
        if (field.value.nonEmpty) Seq("checked".attr := "checked") else Seq.empty[Frag]
      )
  }

}
