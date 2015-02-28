package julienrf.forms.ui

import julienrf.forms.FormData
import julienrf.forms.rules._

case class Field(name: String, tpe: String, value: String, validationAttrs: Map[String, String], errors: Seq[Throwable])

object Field {

  def apply[A : InputType : Mandatory](name: String, rule: Rule[(FormData, String), A]): Field = {
    Field(name, InputType[A].tpe, "", validationAttrs(rule), Nil)
  }

  def validationAttrs[A : Mandatory](rule: Rule[_, A]): Map[String, String] =
    if (Mandatory[A].value) validationAttrsFromRules(rule) + ("required" -> "required")
    else validationAttrsFromRules(rule)

  def validationAttrsFromRules(rule: Rule[_, _]): Map[String, String] =
    rule match {
      case AndThen(lhs, rhs) => validationAttrsFromRules(lhs) ++ validationAttrsFromRules(rhs)
      //      case And(lhs, rhs) => validationAttrsFromRules(lhs) ++ validationAttrsFromRules(rhs)
      case Min(num) => Map("min" -> num.toString)
      case Opt(rule) => validationAttrsFromRules(rule)
      case Head | /*Id() |*/ ToInt | Or(_, _) => Map.empty
    }

}

