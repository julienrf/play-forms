package julienrf.forms.presenters

import ScalaTags.bundle._
import julienrf.forms._
import julienrf.forms.codecs.Codec._
import julienrf.forms.codecs.Constraint.{Min, And}
import julienrf.forms.codecs._

object Input {

  def input[A : Mandatory : InputType]: Presenter[A] = input[A]()

  def input[A : Mandatory : InputType](additionalAttrs: (String, String)*): Presenter[A] = new Presenter[A] {
    def render(field: Field[A]) =
      FormUi(Seq(
        <.input(
          %.`type` := InputType[A].tpe,
          %.name := field.key,
          %.value := field.value.headOption.getOrElse(""),
          (Input.validationAttrs(field.codec) ++ additionalAttrs).map { case (n, v) => n.attr := v }.to[Seq]
        )
      ))
  }

  def validationAttrs[A: Mandatory](codec: Codec[_, A]): Map[String, String] =
    if (Mandatory[A].value) validationAttrsFromCodecs(codec) + ("required" -> "required")
    else validationAttrsFromCodecs(codec)

  // TODO Make this extensible
  def validationAttrsFromCodecs(codec: Codec[_, _]): Map[String, String] =
    codec match {
      case AndThen(lhs, rhs) => validationAttrsFromCodecs(lhs) ++ validationAttrsFromCodecs(rhs)
      case And(lhs, rhs) => validationAttrsFromCodecs(lhs) ++ validationAttrsFromCodecs(rhs)
      case Min(num) => Map("min" -> num.toString)
      case Opt(codec) => validationAttrsFromCodecs(codec)
      case Head | ToInt | ToBoolean | OrElse(_, _) | OneOf(_) | SeveralOf(_) => Map.empty
    }

  def options(data: Seq[(String, String)])(fieldValue: Seq[String]): Seq[scalatags.Text.Tag] =
    for ((value, label) <- data) yield {
      <.option(%.value := value, if (fieldValue contains value) Seq("selected".attr := "selected") else Seq.empty[Modifier])(label)
    }

  def select[A : Mandatory : Multiple](opts: Seq[String] => Seq[scalatags.Text.Tag]): Presenter[A] = new Presenter[A] {
    def render(field: Field[A]) =
      FormUi(Seq(
        <.select(
          %.name := field.key,
          if (Mandatory[A].value) Seq(%.required := "required") else Seq.empty[Modifier],
          if (Multiple[A].value) Seq("multiple".attr := "multiple") else Seq.empty[Modifier]
        )(
          opts(field.value)
        )
      ))
  }

  // TODO Do not add the empty first choice in the case of a multiple select
  def enumOptions[A](values: Set[A], keys: A => String, labels: A => String): Seq[(String, String)] =
    ("" -> "") +: (values.to[Seq] map (a => keys(a) -> labels(a)))


  val checkbox: Presenter[Boolean] = checkbox()

  def checkbox(additionalAttrs: (String, String)*): Presenter[Boolean] = new Presenter[Boolean] {
    def render(field: Field[Boolean]) =
      FormUi(Seq(
        <.input(
          %.`type` := "checkbox",
          %.name := field.key,
          %.value := "true",
          additionalAttrs.map { case (n, v) => n.attr := v}.to[Seq],
          if (field.value.nonEmpty) Seq("checked".attr := "checked") else Seq.empty[Modifier]
        )
      ))
  }

}