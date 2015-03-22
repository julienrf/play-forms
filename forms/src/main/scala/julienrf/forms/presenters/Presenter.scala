package julienrf.forms.presenters

import julienrf.forms.{FieldData, FormUi}
import julienrf.forms.codecs.Codec

trait Presenter[A] {
  def render(field: Field[A]): FormUi  // TODO Abstract over FormUi
}

case class Field[A](name: String, codec: Codec[_, A], value: FieldData, errors: Seq[Throwable])
