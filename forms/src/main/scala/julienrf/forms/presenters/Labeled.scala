package julienrf.forms.presenters

import julienrf.forms.FormUi

/**
 * Fields with just an input and a label
 */
object Labeled {

  import ScalaTags.Bundle._

  def input[A : Mandatory : InputType](label: String): Presenter[A] =
    left(label, Input.input[A])

  def checkbox(label: String): Presenter[Boolean] =
    right(Input.checkbox, label)

  def left[A](label: String, input: Presenter[A]): Presenter[A] = new Presenter[A] {
    def render(field: Field[A]): FormUi = FormUi(Seq(
      <.label(label, input.render(field).html)
    ))
  }

  def right[A](input: Presenter[A], label: String): Presenter[A] = new Presenter[A] {
    def render(field: Field[A]): FormUi = FormUi(Seq(
      <.label(input.render(field).html, label)
    ))
  }

}
