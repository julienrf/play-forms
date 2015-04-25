package julienrf.forms.scalatags

import julienrf.forms.{Mandatory, InputType, Field, Presenter}

/**
 * Fields with just an input and a label
 */
object Labeled {

  import ScalaTags.Bundle._

  def input[A : Mandatory : InputType](label: String): Presenter[A, Frag] =
    left(label, Input.input[A])

  def checkbox(label: String): Presenter[Boolean, Frag] =
    right(Input.checkbox, label)

  def left[A](label: String, input: Presenter[A, Frag]): Presenter[A, Frag] = new Presenter[A, Frag] {
    def render(field: Field[A]): Frag =
      <.label(label, input.render(field))
  }

  def right[A](input: Presenter[A, Frag], label: String): Presenter[A, Frag] = new Presenter[A, Frag] {
    def render(field: Field[A]): Frag =
      <.label(input.render(field), label)
  }

}
