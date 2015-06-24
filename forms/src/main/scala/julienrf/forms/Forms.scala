package julienrf.forms

import julienrf.forms.codecs.Codec
import play.api.libs.functional.{FunctionalCanBuild, InvariantFunctor, ~}
import play.api.mvc.{BodyParsers, BodyParser, Result}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Defines [[julienrf.forms.Forms.Form forms]].
 */
trait Forms {

  /**
   * Type of the output when a form is rendered.
   *
   * For instance, when you use [[julienrf.forms.twirl.Form twirl forms]], this type is fixed to `play.twirl.api.Html`
   */
  type Out

  /**
   * An HTML form with one or more fields. It provides the following features:
   *  - decode the form data to an `A` value ;
   *  - display a filled or empty form with and without validation errors.
   *
   * See the [[Form$ companion object’s documentation]] to know how to create a `Form[A, B]` value.
   */
  sealed trait Form[A] {
    /**
     * Attempts to decode the form data to an `A` value.
     *
     * @param data Form data
     * @return a `Right` value containing the decoded value, or a `Left` value presenting the form (filled with the form
     *         data and showing validation errors).
     */
    def decode(data: FormData): Either[Out, A]

    /**
     * Presents the form (with fields pre-filled with data of the specified value).
     *
     * @param value value to fill the form with, or `None` if the form is not filled
     */
    def render(value: Option[A]): Out

    /**
     * Presents the form (with empty — not filled — fields). Shorthand for `render(None)`.
     */
    def empty: Out = render(None)

    /**
     * Presents the form with pre-filled data. Shorthand for `render(Some(value))`
     */
    def fill(value: A): Out = render(Some(value))

    /**
     * All the keys of `this` `Form`
     */
    def keys: Seq[String]

    def bodyParser(errorHandler: Out => Future[Result])(implicit ec: ExecutionContext): BodyParser[A] =
      BodyParsers.parse.urlFormEncoded.validateM(data => decode(data) match {
        case Left(errors) => errorHandler(errors).map(Left(_))
        case Right(a) => Future.successful(Right(a))
      })
  }

  /**
   * You can create a form using the [[Form$#field field]] method or by combining forms together. forms can be
   * combined in ways that allow you to reuse both the decoding logic and the associated
   * form presentation (the HTML markup) of smaller forms to build larger forms.
   *
   * For instance, consider the following `Place` class definition that defines an abstract representation of your form data:
   *
   * {{{
   *   case class Place(name: String, position: Position)
   *   case class Position(latitude: BigDecimal, longitude: BigDecimal)
   * }}}
   *
   * You can first create two `Form[BigDecimal]` values for the `latitude` and `longitude` fields as follows:
   *
   * {{{
   *   val latitudeForm = Form.field("latitude", Codec.bigDecimal)(Input.input)
   *   val longitudeForm = Form.field("longitude", Codec.bigDecimal)(Input.input)
   * }}}
   *
   * You can then combine them to build a `Form[Position]` as follows:
   *
   * {{{
   *   import play.api.libs.functional.syntax._
   *
   *   val positionForm = (latitudeForm ~ longitudeForm)(Position.apply, unlift(Position.unapply))
   * }}}
   *
   * Finally, you can combine the `positionForm` with a `nameForm` (whose definition has been elided for the sake of brevity)
   * to build a `Form[Place]` as follows:
   *
   * {{{
   *   val placeForm = (
   *     nameForm ~
   *     Form.form("position", positionForm)
   *   )(Place.apply, unlift(Place.unapply))
   * }}}
   *
   */
  object Form {

    /**
     * Creates a form with one field.
     *
     * @param key field key. Each field within a form must have a unique key.
     * @param codec the `Codec` to use to decode and encode the `A` value.
     * @param presenter the `Presenter` to use to render the returned form.
     * @tparam A the type of the data decoded by the returned form.
     */
    def field[A](key: String, codec: Codec[FieldData, A])(presenter: Presenter[A, Out]): Form[A] =
      FieldForm(key, codec, presenter)

    /**
     * Prefixes the fields of the specified form.
     *
     * {{{
     *   val fooForm = Form.field("foo", Codec.text)(Input.input)
     *   val prefixedForm = Form.form("bar", fooForm)
     *   assert(prefixedForm.keys == Seq("bar.foo"))
     * }}}
     *
     * @param key the prefix to prepend to all the fields of the `fa` form.
     * @param fa the form whose fields are to be prepended by the prefix
     * @param ev [[SemiGroup]] defining how the form fields are concatenated when rendered
     */
    def form[A](key: String, fa: Form[A])(implicit ev: SemiGroup[Out]): Form[A] = fa match {
      case FieldForm(subKey, codec, presenter) => FieldForm(s"$key.$subKey", codec, presenter)
      case Form.InMap(fa, f1, f2) => Form.InMap(form(key, fa), f1, f2)
      case Form.Apply(fa, fb) => Form.Apply(form(key, fa), form(key, fb))
    }

    /**
     * Typeclass instances.
     *
     * Forms are invariant functors. It means that you can transform a `Form[A]` into a `Form[B]` provided you have
     * two functions `f: A => B` and `g: B => A`.
     *
     * Forms also implement `FunctionalCanBuild`. It means that you can combine several forms “in parallel” (i.e. the
     * decoding processes all the fields in parallel and accumulates all the validation errors).
     *
     * @param ev Defines how form fields are concatenated when rendered.
     */
    implicit def formInstances(implicit ev: SemiGroup[Out]): InvariantFunctor[Form] with FunctionalCanBuild[Form] =
      new InvariantFunctor[Form] with FunctionalCanBuild[Form] {
        def inmap[A, B](fa: Form[A], f1: A => B, f2: B => A): Form[B] = InMap(fa, f1, f2)
        def apply[A, B](fa: Form[A], fb: Form[B]): Form[A ~ B] = Apply(fa, fb)
      }

    // TODO `codec: Codec[Option[FieldData], A]`
    private case class FieldForm[A](key: String, codec: Codec[FieldData, A], presenter: Presenter[A, Out]) extends Form[A] {
      def decode(data: FormData): Either[Out, A] = {
        val value = data.getOrElse(key, Nil)
        codec.decode(value)
          .left.map(errors => presenter.render(Field(key, codec, Some(value), errors)))
      }
      def render(value: Option[A]): Out =
        presenter.render(Field(key, codec, value.flatMap(codec.encode), Nil))
      def keys: Seq[String] = Seq(key)
    }

    private case class InMap[A, B](fa: Form[A], f1: A => B, f2: B => A) extends Form[B] {
      def decode(data: FormData): Either[Out, B] = fa.decode(data) match {
        case Left(errors) => Left(errors)
        case Right(a) => Right(f1(a))
      }
      def render(value: Option[B]): Out = fa.render(value.map(f2))
      def keys: Seq[String] = fa.keys
    }

    private case class Apply[A, B](fa: Form[A], fb: Form[B])(implicit ev: SemiGroup[Out]) extends Form[A ~ B] {
      require((fa.keys intersect fb.keys).isEmpty) // You can not have two fields with the same path
      def decode(data: FormData): Either[Out, A ~ B] = (fa.decode(data), fb.decode(data)) match {
        case (Right(a), Right(b)) => Right(new ~(a, b))
        case (Right(a), Left(es)) => Left(SemiGroup[Out].combine(fa.render(Some(a)), es))
        case (Left(es), Right(b)) => Left(SemiGroup[Out].combine(es, fb.render(Some(b))))
        case (Left(es1), Left(es2)) => Left(SemiGroup[Out].combine(es1, es2))
      }
      def render(value: Option[A ~ B]): Out =
        SemiGroup[Out].combine(fa.render(value.map(_._1)), fb.render(value.map(_._2)))
      def keys: Seq[String] = fa.keys ++ fb.keys
    }

  }

}

// FIXME Reuse an existing SemiGroup definition (e.g. algebra)
// FIXME Or add more methods to customize the rendering process?
/**
 * Defines how to combine `A` values
 */
trait SemiGroup[A] {
  def combine(lhs: A, rhs: A): A
}

object SemiGroup {
  @inline def apply[A: SemiGroup]: SemiGroup[A] = implicitly
}
