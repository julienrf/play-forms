package julienrf.forms

import julienrf.forms.codecs.Codec

/**
 * Defines how to render a form field.
 *
 * The `A` type parameter can be used to perform type-level computations to derive information from the field.
 *
 * @tparam A type of the field to render.
 * @tparam B type of the output
 */
trait Presenter[A, B] { outer =>
  /**
   * Renders the given field
   */
  def render(field: Field[A]): B

  /**
   * Transforms the result of the rendering.
   * @return A presenter that applies `f` to the rendering of this presenter.
   */
  final def transform(f: B => B): Presenter[A, B] = new Presenter[A, B] {
    def render(field: Field[A]): B = f(outer.render(field))
  }

  /**
   * @return A presenter that uses the given `value` as a default, if the field has no value
   */
  final def defaultValue(value: A): Presenter[A, B] = new Presenter[A, B] {
    def render(field: Field[A]): B = outer.render(field.copy(value = field.value orElse field.codec.encode(value)))
  }

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
case class Field[A](key: String, codec: Codec[FieldData, A], value: Option[FieldData], errors: Seq[Throwable])
