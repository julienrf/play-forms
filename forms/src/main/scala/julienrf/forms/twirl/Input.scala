package julienrf.forms.twirl

import julienrf.forms.{Mandatory, InputType, Presenter, Field, Multiple}
import play.twirl.api.Html

object Input extends julienrf.forms.presenters.Input[Html] {

  def inputAttrs[A : Mandatory : InputType](additionalAttrs: (String, String)*): Presenter[A, Html] = new Presenter[A, Html] {
    def render(field: Field[A]): Html = html.input(
      field,
      Mandatory[A].value,
      InputType[A].tpe,
      (validationAttrs(field.codec) ++ additionalAttrs).to[Seq]
    )
  }

  def options(data: Seq[(String, String)])(fieldValue: Seq[String]): Html =
    html.options(data, fieldValue)

  def select[A : Mandatory : Multiple](opts: Seq[String] => Html): Presenter[A, Html] = new Presenter[A, Html] {
    def render(field: Field[A]): Html = html.select(field, Mandatory[A].value, Multiple[A].value, opts)
  }

  def checkboxAttrs(additionalAttrs: (String, String)*): Presenter[Boolean, Html] = new Presenter[Boolean, Html] {
    def render(field: Field[Boolean]): Html = html.checkbox(field, additionalAttrs)
  }

}
