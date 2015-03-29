package julienrf.forms

import julienrf.forms.presenters.Input
import julienrf.forms.codecs.Codec
import julienrf.forms.st.ScalaTags
import julienrf.forms.st.ScalaTags.hasAttr
import org.scalacheck.{Prop, Properties}
import org.scalacheck.Prop._

object FormTest extends Properties("Form") {

  // This test sucks
  property("invariant functor") = forAll { (ss: Seq[String]) =>
    import play.api.libs.functional.syntax._

    def comparable[A](uiOrA: Either[FormUi, A]): Either[String, A] =
      uiOrA.left.map(ui => ScalaTags.render(ui.html))

    def equal[A](form1: Form[A], form2: Form[A], data: FormData, a: A) =
      ScalaTags.render(form1.empty.html) == ScalaTags.render(form2.empty.html) &&
      comparable(form1.decode(data)) == comparable(form2.decode(data)) &&
      ScalaTags.render(form1.render(a).html) == ScalaTags.render(form2.render(a).html) &&
      form1.keys == form2.keys

    def id[A](form: Form[A], data: FormData, a: A) =
      equal(form.inmap(identity[A], identity[A]), form, data, a)

    def compose[A, B, C](form: Form[A], f1: A => B, f2: B => A, g1: B => C, g2: C => B, data: FormData, c: C) =
      equal(form.inmap(f1, f2).inmap(g1, g2), form.inmap(g1 compose f1, f2 compose g2), data, c)

    val form = Form.field("foo", Codec.text)(Input.input)
    id(form, Map("foo" -> ss), "bar")
    compose(form, (s: String) => s.length, (n: Int) => n.toString, (n: Int) => n % 2 == 0, (b: Boolean) => if (b) 1 else 2, Map("foo" -> ss), true)
  }

  property("apply") = forAll { (ss1: Seq[String], ss2: Seq[String]) =>
    import play.api.libs.functional.syntax._

    def apply[A, B](form1: Form[A], form2: Form[B], data: FormData, a: A, b: B) = {
      val form3 = (form1 ~ form2).tupled
      form3.keys == form1.keys ++ form2.keys &&
      ((form1.decode(data), form2.decode(data), form3.decode(data)) match {
        case (Right(a), Right(b), Right((aa, bb))) => a == aa && b == bb
        case (Left(es), Right(b), Left(ees))       => ScalaTags.render(ees.html) == ScalaTags.render(es.html ++ form2.render(b).html)
        case (Right(a), Left(es), Left(ees))       => ScalaTags.render(ees.html) == ScalaTags.render(form1.render(a).html ++ es.html)
        case (Left(es1), Left(es2), Left(es3))     => ScalaTags.render(es3.html) == ScalaTags.render((es1 ++ es2).html)
        case _                                     => false
      }) &&
      ScalaTags.render(form3.render((a, b)).html) == ScalaTags.render(form1.render(a).html ++ form2.render(b).html) &&
      ScalaTags.render(form3.empty.html) == ScalaTags.render(form1.empty.html ++ form2.empty.html)
    }

    val form1 = Form.field("bar", Codec.text)(Input.input)
    val form2 = Form.field("baz", Codec.text)(Input.input)

    apply(form1, form2, Map("bar" -> ss1, "baz" -> ss2), "value1", "value2")
  }

  property("nesting") = {
    import play.api.libs.functional.syntax._

    def nest[A](prefix: String, form: Form[A]) =
      Form.form(prefix, form).keys == form.keys.map(key => prefix ++ "." ++ key)

    val form1 = Form.field("bar", Codec.text)(Input.input)
    val form2 = Form.field("baz", Codec.text)(Input.input)

    nest("foo", form1) &&
    nest("foo", (form1 ~ form2).tupled)
  }

  property("bind") = {
    def erroneousSubmissionPrefillsFormWithInputValue[A](form: Form[A], data: FormData): Prop = form.decode(data) match {
      case Left(errors) =>
        data.values.flatten.forall(value => hasAttr("value", Some(value))(errors.html))
      case Right(a) => proved
    }

    val formNumber = Form.field("foo", Codec.int)(Input.input)
    erroneousSubmissionPrefillsFormWithInputValue(formNumber, Map("foo" -> Seq("not a number")))
  }

}
