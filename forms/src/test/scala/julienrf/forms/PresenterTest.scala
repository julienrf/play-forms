package julienrf.forms

import org.scalacheck.Properties

object PresenterTest extends Properties("Presenter") {

  property("transform") = {
    def transform[A, B](presenter: Presenter[A, B], field: Field[A], f: B => B): Boolean =
      presenter.transform(f).render(field) == f(presenter.render(field))

    val p = new Presenter[Int, Seq[String]] {
      def render(field: Field[Int]): Seq[String] = field.value
    }
    transform(p, Field("foo", codecs.Codec.int, Seq("bar", "baz"), Nil), (_: FieldData).reverse)
  }
}
