package julienrf.forms.presenters

import scalatags.Text.{Aggregate, Cap}
import scalatags.generic.Attrs
import scalatags.text.{Builder, Tags}

object ScalaTags {

  trait Attr {
    object at extends Cap with Attrs[Builder, String, String]
  }

  trait Tag {
    object < extends Cap with Tags
  }

  trait Implicits extends Aggregate with Cap

  object bundle extends Attr with Tag with Implicits

}
