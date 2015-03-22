package julienrf.forms

import julienrf.forms.presenters.Input
import julienrf.forms.codecs.Codec
import julienrf.forms.st.ScalaTags.hasAttr
import org.scalacheck.{Prop, Properties}
import org.scalacheck.Prop._

object FormTest extends Properties("Form") {

  property("invariant functor") = undecided

  property("applicative") = undecided

  property("nesting") = undecided

  property("empty") = undecided

  property("unbind") = undecided

  property("bind") = {
    def erroneousSubmissionPrefillsFormWithInputValue[A](form: Form[A], data: FormData): Prop = form.bind(data) match {
      case Left(errors) =>
        data.values.flatten.forall(value => hasAttr("value", Some(value))(errors.html))
      case Right(a) => proved
    }

    val formNumber = Form.field("foo", Codec.int)(Input.input)
    erroneousSubmissionPrefillsFormWithInputValue(formNumber, Map("foo" -> Seq("not a number")))
  }

}
