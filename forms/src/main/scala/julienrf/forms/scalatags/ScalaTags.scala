package julienrf.forms.scalatags

import julienrf.forms.SemiGroup
import play.api.mvc.Call

import _root_.scalatags.Text.{Aggregate, Cap, TypedTag}
import _root_.scalatags.generic.Attrs
import _root_.scalatags.text.{Builder, Tags}

object ScalaTags {

  trait Attr {
    object attr extends Cap with Attrs[Builder, String, String]// with InputAttrs[Builder, String, String]
    final val % = attr
  }

  trait Tag {
    object tag extends Cap with Tags
    final val < = tag

    type Tag = TypedTag[String]
  }

  trait Implicits extends Aggregate with Cap {
    implicit val modifierSemiGroup: SemiGroup[Frag] = new SemiGroup[Frag] {
      def combine(lhs: Frag, rhs: Frag): Frag = Seq(lhs, rhs)
    }
  }

  trait Bundle extends Attr with Tag with Implicits
  object Bundle extends Bundle


  def form(call: Call)(modifiers: Bundle.Modifier*): Bundle.Tag = {
    import Bundle._
    <.form(%.method := call.method, %.action := call.url)(modifiers: _*)
  }

}
