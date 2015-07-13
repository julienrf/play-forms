package julienrf.forms

import julienrf.forms.codecs.Codec
import org.scalacheck.{Prop, Properties}
import org.scalacheck.Prop._

object FormTest extends Properties("Form") with Forms {

  type Out = Seq[String]

  def presenter[A]: Presenter[A, Out] = new Presenter[A, Out] {
    def render(field: Field[A]): Out = Seq(field.value.toString())
  }

  implicit def seqSemiGroup[A]: SemiGroup[Seq[A]] = new SemiGroup[Seq[A]] {
    def combine(lhs: Seq[A], rhs: Seq[A]): Seq[A] = lhs ++ rhs
  }

  // This test sucks
  property("invariant functor") = forAll { (ss: Seq[String]) =>
    import play.api.libs.functional.syntax._

    def equal[A](form1: Form[A], form2: Form[A], data: FormData, a: A): Boolean =
      form1.empty == form2.empty &&
      form1.decode(data) == form2.decode(data) &&
      form1.fill(a) == form2.fill(a) &&
      form1.keys == form2.keys

    def id[A](form: Form[A], data: FormData, a: A): Boolean =
      equal(form.inmap(identity[A], identity[A]), form, data, a)

    def compose[A, B, C](form: Form[A], f1: A => B, f2: B => A, g1: B => C, g2: C => B, data: FormData, c: C): Boolean =
      equal(form.inmap(f1, f2).inmap(g1, g2), form.inmap(g1 compose f1, f2 compose g2), data, c)

    val form = Form.field("foo", Codec.text)(presenter)
    id(form, Map("foo" -> ss), "bar")
    compose(form, (s: String) => s.length, (n: Int) => n.toString, (n: Int) => n % 2 == 0, (b: Boolean) => if (b) 1 else 2, Map("foo" -> ss), true)
  }

  property("apply") = forAll { (ss1: Seq[String], ss2: Seq[String]) =>
    import play.api.libs.functional.syntax._

    def apply[A, B](form1: Form[A], form2: Form[B], data: FormData, a: A, b: B): Boolean = {
      val form3 = (form1 ~ form2).tupled
      form3.keys == form1.keys ++ form2.keys &&
      ((form1.decode(data), form2.decode(data), form3.decode(data)) match {
        case (Right(a), Right(b), Right((aa, bb))) => a == aa && b == bb
        case (Left(es), Right(b), Left(ees))       => ees == es ++ form2.fill(b)
        case (Right(a), Left(es), Left(ees))       => ees == form1.fill(a) ++ es
        case (Left(es1), Left(es2), Left(es3))     => es3 == es1 ++ es2
        case _                                     => false
      }) &&
      form3.fill((a, b)) == form1.fill(a) ++ form2.fill(b) &&
      form3.empty == form1.empty ++ form2.empty
    }

    val form1 = Form.field("bar", Codec.text)(presenter)
    val form2 = Form.field("baz", Codec.text)(presenter)

    apply(form1, form2, Map("bar" -> ss1, "baz" -> ss2), "value1", "value2")
  }

  property("nesting") = {
    import play.api.libs.functional.syntax._

    def nest[A](prefix: String, form: Form[A]): Boolean =
      Form.form(prefix, form).keys == form.keys.map(key => prefix ++ "." ++ key)

    val form1 = Form.field("bar", Codec.text)(presenter)
    val form2 = Form.field("baz", Codec.text)(presenter)

    nest("foo", form1) &&
    nest("foo", (form1 ~ form2).tupled)
  }

  property("bind") = {
    def erroneousSubmissionPrefillsFormWithInputValue[A](form: Form[A], data: FormData): Prop = form.decode(data) match {
      case Left(errors) =>
        data.values.forall(values => values.forall(value => errors.exists(_.containsSlice(value))))
      case Right(a) => proved
    }

    val formNumber = Form.field("foo", Codec.int)(presenter)
    erroneousSubmissionPrefillsFormWithInputValue(formNumber, Map("foo" -> Seq("not a number")))
  }

}
