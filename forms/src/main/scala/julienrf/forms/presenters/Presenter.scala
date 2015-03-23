package julienrf.forms.presenters

import julienrf.forms.{FieldData, FormUi}
import julienrf.forms.codecs.Codec

/**
 *
 * @tparam A
 */
trait Presenter[A] {

  def render(field: Field[A]): FormUi  // TODO Abstract over FormUi

}

/**
 *
 * @param name
 * @param codec
 * @param value
 * @param errors
 * @tparam A
 */
case class Field[A](name: String, codec: Codec[_, A], value: FieldData, errors: Seq[Throwable])
