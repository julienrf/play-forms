package julienrf.forms.ui

import julienrf.forms.FormUi


object Select {
  import scalatags.Text.{attrs, tags}
  import scalatags.Text.all._

  def options(data: Seq[(String, String)])(field: Input.Field): Seq[scalatags.Text.Tag] =
    for ((value, label) <- data) yield {
      tags.option(attrs.value := value, if (field.value == value) Seq("selected".attr := "selected") else Seq.empty[Modifier])(label)
    }

  def select(opts: Input.Field => Seq[scalatags.Text.Tag])(field: Input.Field): FormUi = {
    FormUi(Seq(
      tags.select(attrs.name := field.name, field.validationAttrs.map { case (n, v) => n.attr := v }.to[Seq])(
        opts(field)
      )
    ))
  }

  def enumOptions[A](values: Set[A], keys: A => String, labels: A => String): Seq[(String, String)] =
    ("" -> "") +: (values.to[Seq] map (a => keys(a) -> labels(a)))

}
