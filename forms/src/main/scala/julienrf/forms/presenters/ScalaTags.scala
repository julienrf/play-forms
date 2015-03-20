package julienrf.forms.presenters

import scalatags.Text.{TypedTag, Aggregate, Cap}
import scalatags.generic.Attrs
import scalatags.text.{Builder, Tags}

object ScalaTags {

  trait Attr {
    object attr extends Cap with Attrs[Builder, String, String]
    final val % = attr
  }

  trait Tag {
    object tag extends Cap with Tags
    final val < = tag

    type Tag = TypedTag[String]
  }

  trait Implicits extends Aggregate with Cap

  object bundle extends Attr with Tag with Implicits

}
