package julienrf.forms.twirl

import julienrf.forms.{Field, Presenter, InputType, Mandatory}
import play.twirl.api.Html

object TypedField {

  def input[A : Mandatory : InputType](label: Html, inputAttrs: (String, String)*): Presenter[A, Html] =
    Presenter.flatten { field =>
      Field.input[A](
        InputType[A].tpe,
        label,
        (julienrf.forms.presenters.Control.validationAttrs(field.codec) ++ inputAttrs).to[Seq]: _*
      )
    }

  // TODO Use Control.validationAttrs to handle validation attributes
  def radios[A : Mandatory](label: Html, choices: Map[A, (String, String)]): Presenter[A, Html] =
    new Presenter[A, Html] {
      def render(field: Field[A]): Html =
        html"""
          <div class="field radios">
            <div class="label">$label</div>
            ${
              for ((choice, (value, label)) <- choices) yield
                html"""
                  <label><input type="radio" name="${field.key}" value="$value" ${ if (field.value.contains(Seq(value))) "checked" else "" } ${ if (Mandatory[A].value) "required" else "" } /> $label</label>
                """
            }
          </div>
        """
    }

  // TODO validationAttrs
  def textarea[A : Mandatory](label: Html, width: Option[Int] = None, height: Option[Int] = None): Presenter[A, Html] =
    new Presenter[A, Html] {
      def render(field: Field[A]): Html =
        Field.layout(
          label,
          html"""
            <textarea name="${field.key}" ${width.fold(html"")(w => html"width='$w'")} ${height.fold(html"")(h => html"height='$h'")}>${field.value}</textarea>
          """
        )
    }

}
