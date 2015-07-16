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
        layout(label, inputHtml(tpe, field, inputAttrs))
    }

  def layout(label: Html, control: Html): Html =
    html"""
      <label class="field">
        <span class="label">$label</span>
        $control
      </label>
    """

  def inputHtml(tpe: String, field: julienrf.forms.Field[_], attrs: Seq[(String, String)]): Html =
    html"""
      <input type="$tpe" name="${field.key}" value="${field.value.flatMap(_.headOption).getOrElse("")}" ${attrs.map { case (k, v) => html"$k='$v'" }} />
    """

  def checkbox(label: Html, inputAttrs: (String, String)*): Presenter[Boolean, Html] =
    new Presenter[Boolean, Html] {
      def render(field: Field[Boolean]): Html =
        html"""
          <label class="field">
            ${inputHtml("checkbox", field, inputAttrs)} $label
          </label>
        """
    }

}
