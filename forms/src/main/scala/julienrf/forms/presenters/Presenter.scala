package julienrf.forms.presenters

import julienrf.forms.FormUi
import julienrf.forms.rules.Rule

// TODO Use a Reader
trait Presenter[A] {

  def render(name: String, rule: Rule[_, A], value: Option[String], errors: Seq[Throwable]): FormUi  // TODO Abstract over FormUi

}