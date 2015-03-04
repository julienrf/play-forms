package julienrf.forms.ui

import julienrf.forms._
import julienrf.forms.rules._

// FIXME Use a Reader[Field]?
object Input {

  import scalatags.Text.{attrs, tags}
  import scalatags.Text.all._

  def input(field: Field): FormUi = {
    FormUi(Seq(
      tags.input(
        attrs.`type` := field.tpe,
        attrs.name := field.name,
        attrs.value := field.value,
        field.validationAttrs.map { case (n, v) => n.attr := v}.to[Seq]
      )
    ))
  }

  case class Field(name: String, tpe: String, value: String, validationAttrs: Map[String, String], errors: Seq[Throwable])

  object Field {

    def apply[A: InputType : Mandatory](name: String, rule: Rule[(FormData, String), A]): Field = {
      Field(name, InputType[A].tpe, "", validationAttrs(rule), Nil)
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

  }

}