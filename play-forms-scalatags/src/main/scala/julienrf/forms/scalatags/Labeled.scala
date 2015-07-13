package julienrf.forms.scalatags

import julienrf.forms.{Mandatory, InputType, Field, Presenter}

/**
 * Fields with just an input and a label
 */
object Labeled {

  import ScalaTags.Bundle._

  def input[A : Mandatory : InputType](label: String): Presenter[A, Frag] =
    left(label, Control.input[A])

  def checkbox(label: String): Presenter[Boolean, Frag] =
    right(Control.checkbox, label)

  /**
   * @return a `label` tag containing the label and the control’s HTML fragment.
   */
  def left[A](control: String, input: Presenter[A, Frag]): Presenter[A, Frag] = new Presenter[A, Frag] {
    def render(field: Field[A]): Frag =
      <.label(control, input.render(field))
  }

  /**
   * @return a `label` tag containing the control’s HTML fragment and the label.
   */
  def right[A](control: Presenter[A, Frag], label: String): Presenter[A, Frag] = new Presenter[A, Frag] {
    def render(field: Field[A]): Frag =
      <.label(control.render(field), label)
  }

}
