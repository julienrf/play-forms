package julienrf.forms.presenters

import julienrf.forms._
import julienrf.forms.codecs.Codec.{AndThen, Opt}
import julienrf.forms.codecs.Constraint.Min
import julienrf.forms.codecs.{Codec, Error}

/**
 * Produces HTML similar to what form helpers built-in with Play produce, but with the following changes:
 *  - Fixes the problem with `optional(nonEmptyText)`
 *  - Adds HTML validation attributes to input tag
 */
abstract class PlayField[Out](input: Input[Out]) {
  /**
   * Similar to Play’s `inputText` or `inputDate`. It automatically sets the input type according
   * to the type parameter `A`. It works with numbers too.
   */
  // TODO Handle id, help, showConstraints, error, showErrors and additionalInputAttrs
  def input[A : Mandatory : InputType](label: String): Presenter[A, Out] =
    withPresenter(field => input.inputAttrs[A]("id" -> field.key), label)

  def select[A : Mandatory : Multiple](label: String, opts: Seq[String] => Out): Presenter[A, Out] =
    withPresenter(field => input.select[A](opts), label)

  def checkbox(label: String): Presenter[Boolean, Out]

  def withPresenter[A : Mandatory](inputPresenter: Field[A] => Presenter[A, Out], label: String): Presenter[A, Out]

  def layout(field: Field[_])(dtContent: Out*)(dds: Out*): Out

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
