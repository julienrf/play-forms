package julienrf.forms.presenters

import julienrf.forms.FormUi
import julienrf.forms.codecs.Codec

trait Presenter[A] {
  def render(field: Field[A]): FormUi  // TODO Abstract over FormUi
}

case class Field[A](name: String, codec: Codec[_, A], value: Seq[String], errors: Seq[Throwable])
