package julienrf.forms.presenters

import julienrf.forms.{FieldData, FormUi}
import julienrf.forms.codecs.Codec

/**
 * Defines how to render a form field.
 *
 * The `A` type parameter can be used to define type-level computations to derive information from the field.
 *
 * @tparam A type of the field to render.
 */
trait Presenter[A] {

  def render(field: Field[A]): FormUi  // TODO Abstract over FormUi

}

/**
 * Form field.
 *
 * @param key the key of the field.
 * @param codec the codec of the field.
 * @param value the value of the field.
 * @param errors the validation errors associated to the field.
 * @tparam A type of the field.
 */
case class Field[A](key: String, codec: Codec[_, A], value: FieldData, errors: Seq[Throwable])
