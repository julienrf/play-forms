package julienrf.forms.presenters

import julienrf.forms.FormUi
import julienrf.forms.rules._

/**
 * Produces HTML similar to what form helpers built-in with Play produce, but with the following changes:
 *  - Fixes the problem with `optional(nonEmptyText)`
 *  - Adds HTML validation attributes to input tag
 */
object PlayField {

  import ScalaTags.bundle._

  /**
   * Similar to Play’s `inputText` or `inputDate`. It automatically sets the input type according
   * to the type parameter `A`. It works with numbers too.
   */
  // TODO Handle id, help, showConstraints, error, showErrors and additionalInputAttrs
  def input[A : Mandatory : InputType](label: String): Presenter[A] =
    withPresenter(field => Input.input[A]("id" -> field.name), label)

  def select[A : Mandatory](label: String, opts: Option[String] => Seq[scalatags.Text.Tag]): Presenter[A] =
    withPresenter(field => Input.select[A](opts), label)

  def withPresenter[A : Mandatory](inputPresenter: Field[A] => Presenter[A], label: String): Presenter[A] = new Presenter[A] {
    def render(field: Field[A]) =
      FormUi(Seq(
        <.dl((if (field.errors.nonEmpty) Seq(%.`class` := "error") else Nil): _*)(
          <.dt(<.label(%.`for` := field.name)(label)), // TODO Generate a random id
          <.dd(inputPresenter(field).render(field).html),
          for (error <- field.errors) yield <.dd(%.`class` := "error")(errorToMessage(error)),
          for (info <- infos(field.rule)) yield <.dd(%.`class` := "info")(info)
        )
      ))
  }

  // TODO Use i18n
  def errorToMessage(error: Throwable): String = error match {
    case Error.Required => "This field is required"
    case Error.MustBeAtLeast(n) => s"Must be greater or equal to $n"
    case _ => "Invalid"
  }

  // TODO Use i18n
  def infos[A : Mandatory](rule: Rule[_, A]): Seq[String] =
    if (Mandatory[A].value) "Required" +: infosFromRules(rule)
    else infosFromRules(rule)

  // TODO Extensibility
  def infosFromRules(rule: Rule[_, _]): Seq[String] = rule match {
    case AndThen(lhs, rhs) => infosFromRules(lhs) ++ infosFromRules(rhs)
    case Min(num) => Seq(s"Minimum value: $num")
    case Opt(rule) => infosFromRules(rule)
    case _ => Nil
  }

}
