package julienrf.forms.scalatags

import julienrf.forms._
import ScalaTags.Bundle._

object PlayField extends julienrf.forms.presenters.PlayField[Frag](Input) {

  def checkbox(label: String): Presenter[Boolean, Frag] = new Presenter[Boolean, Frag] {
    def render(field: Field[Boolean]): Frag =
      layout(field)()(
        <.dd(
          Input.checkboxAttrs("id" -> field.key).render(field),
          <.label(%.`for` := field.key)(label)
        )
      )
  }

  def withPresenter[A : Mandatory](inputPresenter: Field[A] => Presenter[A, Frag], label: String): Presenter[A, Frag] = new Presenter[A, Frag] {
    def render(field: Field[A]): Frag =
      layout(field)(
          <.label(%.`for` := field.key)(label)
      )(
          <.dd(inputPresenter(field).render(field)),
          for (error <- field.errors) yield <.dd(%.`class` := "error")(errorToMessage(error)),
          for (info <- infos(field.codec)) yield <.dd(%.`class` := "info")(info)
      )
  }

  def layout(field: Field[_])(dtContent: Frag*)(dds: Frag*): Frag =
    <.dl((if (field.errors.nonEmpty) Seq(%.`class` := "error") else Nil): _*)(
      <.dt(dtContent),
      dds
    )

}
