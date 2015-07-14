package julienrf.forms

import play.twirl.api.{Format, BaseScalaTemplate, HtmlFormat, Html}

/**
 * `Forms` specialization that renders Twirl’s [[https://www.playframework.com/documentation/2.4.x/api/scala/play/twirl/api/Html.html Html]] values.
 */
package object twirl extends Forms {

  type Out = Html

  implicit val semiGroup: SemiGroup[Out] = new SemiGroup[Out] {
    def combine(lhs: Out, rhs: Out): Out = HtmlFormat.fill(collection.immutable.Seq(lhs, rhs))
  }

  implicit class TwirlInterpolation(val sc: StringContext) extends AnyVal {
    def html(args: Any*): Html = {
      sc.checkLengths(args)
      val array = Array.ofDim[Any](args.size + sc.parts.size)
      val strings = sc.parts.iterator
      val expressions = args.iterator
      array(0) = HtmlFormat.raw(strings.next())
      var i = 1
      while (strings.hasNext) {
        array(i) = expressions.next()
        array(i + 1) = HtmlFormat.raw(strings.next())
        i += 2
      }
      new BaseScalaTemplate[Html, Format[Html]](HtmlFormat)._display_(array)
    }
    def h(args: Any*): Html = html(args: _*)
  }

}
