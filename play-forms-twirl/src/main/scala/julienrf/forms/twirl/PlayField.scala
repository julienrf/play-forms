package julienrf.forms.twirl

import julienrf.forms.{Mandatory, Field, Presenter}
import play.twirl.api.{HtmlFormat, Html}

object PlayField extends julienrf.forms.presenters.PlayField[Html](Control) {

  def checkbox(label: String): Presenter[Boolean, Html] = new Presenter[Boolean, Html] {
    def render(field: Field[Boolean]): Html =
      layout(field)()(html.playFieldCheckboxDd(field, Control.checkboxAttrs("id" -> field.key).render(field), label))
  }

  def withPresenter[A : Mandatory](inputPresenter: Field[A] => Presenter[A, Html], label: String): Presenter[A, Html] = new Presenter[A, Html] {
    def render(field: Field[A]): Html =
      layout(field)(
        HtmlFormat.fill(collection.immutable.Seq(HtmlFormat.raw("<label for=\""), HtmlFormat.escape(field.key), HtmlFormat.raw("\">"), HtmlFormat.escape(label), HtmlFormat.raw("</label>")))
      )(
        html.playFieldDds(field, inputPresenter(field).render(field), infos(field.codec), errorToMessage)
      )
  }

  def layout(field: Field[_])(dtContent: Html*)(dds: Html*): Html =
    html.playFieldLayout(field)(dtContent: _*)(dds: _*)

}
