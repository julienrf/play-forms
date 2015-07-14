package julienrf.forms.twirl

import julienrf.forms.{Field, Presenter}
import play.twirl.api.Html

/**
 * A set of basic presenters.
 */
object Field {

  def inputText(label: Html, inputAttrs: (String, String)*): Presenter[String, Html] =
    input("text", label, inputAttrs: _*)

  def inputNumber[A : Numeric](label: Html, inputAttrs: (String, String)*): Presenter[A, Html] =
    input("number", label, inputAttrs: _*)

  def input[A](tpe: String, label: Html, inputAttrs: (String, String)*): Presenter[A, Html] =
    new Presenter[A, Html] {
      def render(field: Field[A]): Html =
        julienrf.forms.twirl.field.html.layout(
          label,
          julienrf.forms.twirl.field.html.input(tpe, field, inputAttrs)
        )
    }

  def checkbox(label: Html, inputAttrs: (String, String)*): Presenter[Boolean, Html] =
    new Presenter[Boolean, Html] {
      def render(field: Field[Boolean]): Html =
        julienrf.forms.twirl.field.html.checkbox(label, field, inputAttrs)
    }

}
