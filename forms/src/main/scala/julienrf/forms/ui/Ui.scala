package julienrf.forms.ui

import julienrf.forms.Fields
import play.api.mvc.Call

object Ui {
  import scalatags.Text.{attrs, tags, Modifier}
  import scalatags.Text.all._

  def input(field: Field): Fields = {
    Fields(Seq(
      tags.input(
        attrs.`type` := field.tpe,
        attrs.name := field.name,
        attrs.value := field.value,
        field.validationAttrs.map { case (n, v) => n.attr := v }.to[Seq]
      )
    ))
  }

  def form(route: Call)(contents: Modifier*): scalatags.Text.Tag =
    tags.form(attrs.action := route.url, attrs.method := route.method)((contents :+ (tags.button("Submit"))): _*)

}