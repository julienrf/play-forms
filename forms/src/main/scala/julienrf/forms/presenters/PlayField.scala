package julienrf.forms.presenters

import julienrf.forms.codecs.Codec.{AndThen, Opt}
import julienrf.forms.codecs.Constraint.Min
import julienrf.forms.codecs._
import julienrf.forms.FormUi

/**
 * Produces HTML similar to what form helpers built-in with Play produce, but with the following changes:
 *  - Fixes the problem with `optional(nonEmptyText)`
 *  - Adds HTML validation attributes to input tag
 */
object PlayField {

  import ScalaTags.Bundle._

  /**
   * Similar to Play’s `inputText` or `inputDate`. It automatically sets the input type according
   * to the type parameter `A`. It works with numbers too.
   */
  // TODO Handle id, help, showConstraints, error, showErrors and additionalInputAttrs
  def input[A : Mandatory : InputType](label: String): Presenter[A] =
    withPresenter(field => Input.input[A]("id" -> field.key), label)

  def select[A : Mandatory : Multiple](label: String, opts: Seq[String] => Seq[scalatags.Text.Tag]): Presenter[A] =
    withPresenter(field => Input.select[A](opts), label)

  def checkbox(label: String): Presenter[Boolean] = new Presenter[Boolean] {
    def render(field: Field[Boolean]): FormUi =
      layout(field)()(
        <.dd(
          Input.checkbox("id" -> field.key).render(field).html, // TODO Generate a random id
          <.label(%.`for` := field.key)(label)
        )
      )
  }

  def withPresenter[A : Mandatory](inputPresenter: Field[A] => Presenter[A], label: String): Presenter[A] = new Presenter[A] {
    def render(field: Field[A]): FormUi =
      layout(field)(
          <.label(%.`for` := field.key)(label) // TODO Generate a random id
      )(
          <.dd(inputPresenter(field).render(field).html),
          for (error <- field.errors) yield <.dd(%.`class` := "error")(errorToMessage(error)),
          for (info <- infos(field.codec)) yield <.dd(%.`class` := "info")(info)
      )
  }

  def layout(field: Field[_])(dtContent: Modifier*)(dds: Modifier*): FormUi =
    FormUi(Seq(
      <.dl((if (field.errors.nonEmpty) Seq(%.`class` := "error") else Nil): _*)(
        <.dt(dtContent),
        dds
      )
    ))

  // TODO Use i18n
  def errorToMessage(error: Throwable): String = error match {
    case Error.Required => "This field is required"
    case Error.MustBeAtLeast(n) => s"Must be greater or equal to $n"
    case _ => "Invalid"
  }

  // TODO Use i18n
  def infos[A : Mandatory](rule: Codec[_, A]): Seq[String] =
    if (Mandatory[A].value) "Required" +: infosFromRules(rule)
    else infosFromRules(rule)

  // TODO Extensibility
  def infosFromRules(rule: Codec[_, _]): Seq[String] = rule match {
    case AndThen(lhs, rhs) => infosFromRules(lhs) ++ infosFromRules(rhs)
    case Min(num) => Seq(s"Minimum value: $num")
    case Opt(rule) => infosFromRules(rule)
    case _ => Nil
  }

}
