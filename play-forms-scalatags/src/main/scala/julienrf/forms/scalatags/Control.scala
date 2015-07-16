package julienrf.forms.scalatags

import julienrf.forms._
import ScalaTags.Bundle._

object Control extends julienrf.forms.presenters.Control[Frag] {

  def inputAttrs[A : Mandatory : InputType](additionalAttrs: (String, String)*): Presenter[A, Frag] = new Presenter[A, Frag] {
    def render(field: Field[A]): Frag =
      <.input(
        %.`type` := InputType[A].tpe,
        %.name := field.key,
        %.value := field.value.flatMap(_.headOption).getOrElse(""),
        (julienrf.forms.presenters.Control.validationAttrs(field.codec) ++ additionalAttrs).map { case (n, v) => n.attr := v }.to[Seq]
      )
  }

  def options(data: Seq[(String, String)])(field: Field[_]): Frag =
    for ((value, label) <- data) yield {
      <.option(%.value := value, if (field.value.exists(_.contains(value))) Seq("selected".attr := "selected") else Seq.empty[Modifier])(label)
    }

  def select[A : Mandatory : Multiple](opts: Field[A] => Frag): Presenter[A, Frag] = new Presenter[A, Frag] {
    def render(field: Field[A]): Frag =
      <.select(
        %.name := field.key,
        if (Mandatory[A].value) Seq(%.required := "required") else Seq.empty[Modifier],
        if (Multiple[A].value) Seq("multiple".attr := "multiple") else Seq.empty[Modifier]
      )(opts(field))
  }

  def checkboxAttrs(additionalAttrs: (String, String)*): Presenter[Boolean, Frag] = new Presenter[Boolean, Frag] {
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
