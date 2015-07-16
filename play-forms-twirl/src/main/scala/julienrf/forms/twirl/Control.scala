package julienrf.forms.twirl

import julienrf.forms.{Mandatory, InputType, Presenter, Field, Multiple}
import play.twirl.api.Html

object Control extends julienrf.forms.presenters.Control[Html] {

  def inputAttrs[A : Mandatory : InputType](additionalAttrs: (String, String)*): Presenter[A, Html] = new Presenter[A, Html] {
    def render(field: Field[A]): Html = html.input(
      field,
      Mandatory[A].value,
      InputType[A].tpe,
      (julienrf.forms.presenters.Control.validationAttrs(field.codec) ++ additionalAttrs).to[Seq]
    )
  }

  def options(data: Seq[(String, String)])(field: Field[_]): Html =
    html"""
      ${for((value, label) <- data) yield html"""<option value="$value" ${if(field.value.exists(_.contains(value))) "selected" else "" }>$label</option>""" }
    """

  def select[A : Mandatory : Multiple](opts: Field[A] => Html): Presenter[A, Html] = new Presenter[A, Html] {
    def render(field: Field[A]): Html =
      html"""
        <select name="${field.key}" ${if(Mandatory[A].value) "required" else ""} ${if(Multiple[A].value) "multiple" else "" }>
          ${opts(field)}
        </select>
      """
  }

  def checkboxAttrs(additionalAttrs: (String, String)*): Presenter[Boolean, Html] = new Presenter[Boolean, Html] {
    def render(field: Field[Boolean]): Html = html.checkbox(field, additionalAttrs)
  }

}
