package julienrf.forms.twirl

import julienrf.forms.{Presenter, InputType, Mandatory}
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

}
