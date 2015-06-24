package julienrf.forms

import org.scalacheck.Properties

object PresenterTest extends Properties("Presenter") {

  val p = new Presenter[Int, Option[Seq[String]]] {
    def render(field: Field[Int]): Option[Seq[String]] = field.value
  }

  property("transform") = {
    def transform[A, B](presenter: Presenter[A, B], field: Field[A], f: B => B): Boolean =
      presenter.transform(f).render(field) == f(presenter.render(field))

    transform(p, Field("foo", codecs.Codec.int, Some(Seq("bar", "baz")), Nil), (_: FieldData).map(_.reverse))
  }

  property("defaultValue") = {
    val defaultValue = 42
    val filledField = Field("foo", codecs.Codec.int, Some(Seq("bar", "baz")), Nil)
    val emptyField = filledField.copy(value = Option.empty[Seq[String]])
    val pd = p.defaultValue(defaultValue)
    pd.render(emptyField) == Some(Seq("42")) && pd.render(filledField) == Some(Seq("bar", "baz"))
  }

}
