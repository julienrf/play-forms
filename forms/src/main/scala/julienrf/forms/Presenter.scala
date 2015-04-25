package julienrf.forms

import julienrf.forms.codecs.Codec

/**
 * Defines how to render a form field.
 *
 * The `A` type parameter can be used to define type-level computations to derive information from the field.
 *
 * @tparam A type of the field to render.
 * @tparam B type of the output
 */
trait Presenter[A, B] { outer =>
  def render(field: Field[A]): B

  /**
   * Transforms the result of the rendering.
   * @return A presenter that applies `f` to the rendering of this presenter.
   */
  def transform(f: B => B): Presenter[A, B] = new Presenter[A, B] {
    def render(field: Field[A]): B = f(outer.render(field))
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
case class Field[A](key: String, codec: Codec[_, A], value: FieldData, errors: Seq[Throwable])
