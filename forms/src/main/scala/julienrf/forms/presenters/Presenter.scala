package julienrf.forms.presenters

import julienrf.forms.FormUi
import julienrf.forms.rules.Rule

trait Presenter[A] {
  def render(field: Field[A]): FormUi  // TODO Abstract over FormUi
}

case class Field[A](name: String, rule: Rule[_, A], value: Seq[String], errors: Seq[Throwable])
