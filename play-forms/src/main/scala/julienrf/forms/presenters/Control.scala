package julienrf.forms.presenters

import julienrf.forms._
import julienrf.forms.codecs.Codec
import julienrf.forms.codecs.Codec._
import julienrf.forms.codecs.Constraint.{Constrainable, And, GreaterOrEqual}

/**
 * Defines common HTML controls (input and select).
 */
abstract class Control[Out] {

  def input[A : Mandatory : InputType]: Presenter[A, Out] = inputAttrs[A]()

  def inputAttrs[A : Mandatory : InputType](additionalAttrs: (String, String)*): Presenter[A, Out]

  def options(data: Seq[(String, String)])(fieldValue: Seq[String]): Out

  def select[A : Mandatory : Multiple](opts: Seq[String] => Out): Presenter[A, Out]

  // TODO Do not add the empty first choice in the case of a multiple select
  def enumOptions[A](values: Set[A], keys: A => String, labels: A => String): Seq[(String, String)] =
    ("" -> "") +: (values.to[Seq] map (a => keys(a) -> labels(a)))

  val checkbox: Presenter[Boolean, Out] = checkboxAttrs()

  def checkboxAttrs(additionalAttrs: (String, String)*): Presenter[Boolean, Out]

}

object Control {

  def validationAttrs[A: Mandatory](codec: Codec[_, A]): Map[String, String] =
    if (Mandatory[A].value) validationAttrsFromCodecs(codec) + ("required" -> "required")
    else validationAttrsFromCodecs(codec)

  // TODO Make this extensible
  def validationAttrsFromCodecs(codec: Codec[_, _]): Map[String, String] =
    codec match {
      case AndThen(lhs, rhs) => validationAttrsFromCodecs(lhs) ++ validationAttrsFromCodecs(rhs)
      case And(lhs, rhs) => validationAttrsFromCodecs(lhs) ++ validationAttrsFromCodecs(rhs)
      case GreaterOrEqual(num) => Map("min" -> num.toString)
      case Opt(codec) => validationAttrsFromCodecs(codec)
      case Head | ToInt | ToBoolean | OrElse(_, _) | OneOf(_) | SeveralOf(_) | _: Codecable[x, y] | _: Constrainable[z] => Map.empty
    }

}