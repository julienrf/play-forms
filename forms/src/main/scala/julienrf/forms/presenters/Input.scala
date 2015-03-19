package julienrf.forms.presenters

import julienrf.forms._
import julienrf.forms.rules._

// FIXME Use a Reader[Field]?
object Input {

  import scalatags.Text.{attrs, tags}
  import scalatags.Text.all._

  def input[A : Mandatory : InputType]: Presenter[A] = new Presenter[A] {
    case class Field(name: String, value: String, validationAttrs: Map[String, String], errors: Seq[Throwable]) extends FieldLike {
      def addingError(error: Throwable) = copy(errors = errors :+ error)
      def withValue(value: String) = copy(value = value)
    }
    def field(name: String, rule: Rule[(FormData, String), A]) = Field(name, "", validationAttrs(rule), Nil)
    def render(field: Field) =
      FormUi(Seq(inputUi(InputType[A].tpe, field.name, field.value, field.validationAttrs)))
  }

  def inputUi(tpe: String, name: String, value: String, otherAttrs: Map[String, String]): Modifier =
    tags.input(
      attrs.`type` := tpe,
      attrs.name := name,
      attrs.value := value,
      otherAttrs.map { case (n, v) => n.attr := v}.to[Seq]
    )

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