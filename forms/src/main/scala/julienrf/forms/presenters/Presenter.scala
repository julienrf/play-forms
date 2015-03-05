package julienrf.forms.presenters

import julienrf.forms.{FormData, FormUi}
import julienrf.forms.rules.Rule

trait Presenter[A] {

  type Field <: FieldLike

  trait FieldLike {
    def addingError(error: Throwable): Field
    def withValue(value: String): Field
  }

  def field(name: String, rule: Rule[(FormData, String), A]): Field
  def render(field: Field): FormUi // TODO Abstract over FormUi

}
