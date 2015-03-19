package julienrf.forms.presenters

import julienrf.forms.{FormUi, FormData}
import julienrf.forms.rules._

/**
 * Replicates the HTML form helpers built-in with Play
 * Also fixes the problem with `optional(nonEmptyText)`
 */
object PlayField {

  import scalatags.Text.all._
  import scalatags.Text.{attrs, tags}

  // TODO Handle id, help, showConstraints, error, showErrors and additionalInputAttrs
  def inputText[A : Mandatory : InputType](args: (Symbol, String)*): Presenter[A] = new Presenter[A] {
    val argsMap = args.toMap
    case class Field(name: String, value: String, label: Option[String], errors: Seq[Throwable], infos: Seq[String]) extends FieldLike {
      def addingError(error: Throwable) = copy(errors = errors :+ error)
      def withValue(value: String) = copy(value = value)
    }
    def field(name: String, rule: Rule[(FormData, String), A]) = Field(name, "", argsMap.get('_label), Seq.empty, infos(rule))
    def render(field: Field) =
      FormUi(Seq(
        tags.dl(attrs.id := s"${field.name}_field")((if (field.errors.nonEmpty) Seq(attrs.`class` := "error") else Nil): _*)(
          tags.dt(tags.label(attrs.`for` := field.name)(field.label.map(label => label: Modifier))),
          tags.dd(Input.inputUi(InputType[A].tpe, field.name, field.value, Map.empty)),
          for (error <- field.errors) yield tags.dd(attrs.`class` := "error")(errorToMessage(error)),
          for (info <- field.infos) yield tags.dd(attrs.`class` := "info")(info)
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
