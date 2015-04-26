package julienrf.forms

import play.twirl.api.{HtmlFormat, Html}

/**
 * `Forms` specialization that renders Twirl’s [[https://www.playframework.com/documentation/2.4.x/api/scala/play/twirl/api/Html.html Html]] values.
 */
package object twirl extends Forms {

  type Out = Html

  implicit val semiGroup: SemiGroup[Out] = new SemiGroup[Out] {
    def combine(lhs: Out, rhs: Out): Out = HtmlFormat.fill(collection.immutable.Seq(lhs, rhs))
  }
}
