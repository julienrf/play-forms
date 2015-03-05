package julienrf.forms.presenters

import julienrf.forms._
import julienrf.forms.rules.Rule

abstract class Select[A : Mandatory] extends Presenter[A] {
  case class Field(name: String, value: String, errors: Seq[Throwable]) extends FieldLike {
    def addingError(error: Throwable) = copy(errors = errors :+ error)
    def withValue(value: String) = copy(value = value)
  }
  def field(name: String, rule: Rule[(FormData, String), A]) = Field(name, "", Nil)
  def render(field: Field): FormUi
}

object Select {
  import scalatags.Text.{attrs, tags}
  import scalatags.Text.all._

  def options(data: Seq[(String, String)])(fieldValue: String): Seq[scalatags.Text.Tag] =
    for ((value, label) <- data) yield {
      tags.option(attrs.value := value, if (fieldValue == value) Seq("selected".attr := "selected") else Seq.empty[Modifier])(label)
    }

  def select[A : Mandatory](opts: String => Seq[scalatags.Text.Tag]): Select[A] = new Select[A] {
    def render(field: Field) =
      FormUi(Seq(
        tags.select(attrs.name := field.name, if (Mandatory[A].value) Seq(attrs.required := "required") else Seq.empty[Modifier])(
          opts(field.value)
        )
      ))
  }

  def enumOptions[A](values: Set[A], keys: A => String, labels: A => String): Seq[(String, String)] =
    ("" -> "") +: (values.to[Seq] map (a => keys(a) -> labels(a)))

}
