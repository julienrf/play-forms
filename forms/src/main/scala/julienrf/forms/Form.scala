package julienrf.forms

import julienrf.forms.presenters.{Field, Presenter}
import julienrf.forms.codecs.Codec
import play.api.libs.functional.{FunctionalCanBuild, InvariantFunctor, ~}
import play.api.mvc.{BodyParsers, BodyParser, Result}

import scala.concurrent.{ExecutionContext, Future}

/**
 * An HTML form with one or more fields. It provides the following features:
 *  - decode the form data to an `A` value ;
 *  - display an empty (not filled) form to the client ;
 *  - display a filled form with and without validation errors.
 *
 * See the [[Form$ companion object’s documentation]] to know how to create a `Form[A]` value.
 */
// FIXME abstract over FormUi (we just need a SemiGroup)
sealed trait Form[A] {

  /**
   * Attempts to decode the form data to an `A` value.
   *
   * @param data Form data
   * @return a `Right` value containing the decoded value, or a `Left` value presenting the form (filled with the form
   *         data and showing validation errors).
   */
  def decode(data: FormData): Either[FormUi, A]

  /**
   * Presents the form (with fields pre-filled with data of the specified value).
   *
   * @param a value to fill the form with
   */
  def render(a: A): FormUi

  /**
   * Presents the form (with empty — not filled — fields).
   */
  def empty: FormUi

  /**
   * All the keys of `this` `Form`
   */
  def keys: Seq[String]

  def bodyParser(errorHandler: FormUi => Future[Result])(implicit ec: ExecutionContext): BodyParser[A] = BodyParsers.parse.urlFormEncoded.validateM(data => decode(data) match {
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
  def field[A](key: String, codec: Codec[FieldData, A])(presenter: Presenter[A]): Form[A] =
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
   */
  def form[A](key: String, fa: Form[A]): Form[A] = fa match {
    case FieldForm(subKey, codec, presenter) => FieldForm(s"$key.$subKey", codec, presenter)
    case Form.InMap(fa, f1, f2) => Form.InMap(form(key, fa), f1, f2)
    case Form.Apply(fa, fb) => Form.Apply(form(key, fa), form(key, fb))
  }

  /**
   * Typeclass instances
   */
  implicit val formInstances: InvariantFunctor[Form] with FunctionalCanBuild[Form] =
    new InvariantFunctor[Form] with FunctionalCanBuild[Form] {
      def inmap[A, B](fa: Form[A], f1: A => B, f2: B => A) = InMap(fa, f1, f2)
      def apply[A, B](fa: Form[A], fb: Form[B]) = Apply(fa, fb)
    }

  protected final case class FieldForm[A](key: String, codec: Codec[FieldData, A], presenter: Presenter[A]) extends Form[A] {
    def decode(data: FormData) = {
      val value = data.getOrElse(key, Nil)
      codec.decode(value)
        .left.map(errors => presenter.render(Field(key, codec, value, errors)))
    }
    def render(a: A) = presenter.render(Field(key, codec, codec.encode(a) getOrElse Nil, Nil))
    def empty = presenter.render(Field(key, codec, Nil, Nil))
    def keys = Seq(key)
  }

  protected final case class InMap[A, B](fa: Form[A], f1: A => B, f2: B => A) extends Form[B] {
    def decode(data: FormData) = fa.decode(data) match {
      case Left(errors) => Left(errors)
      case Right(a) => Right(f1(a))
    }
    def render(b: B) = fa.render(f2(b))
    def empty = fa.empty
    def keys = fa.keys
  }

  protected final case class Apply[A, B](fa: Form[A], fb: Form[B]) extends Form[A ~ B] {
    require((fa.keys intersect fb.keys).isEmpty) // You can not have two fields with the same path
    def decode(data: FormData) = (fa.decode(data), fb.decode(data)) match {
      case (Right(a), Right(b)) => Right(new ~(a, b))
      case (Right(a), Left(es)) => Left(fa.render(a) ++ es)
      case (Left(es), Right(b)) => Left(es ++ fb.render(b))
      case (Left(es1), Left(es2)) => Left(es1 ++ es2)
    }
    def render(ab: A ~ B) = fa.render(ab._1) ++ fb.render(ab._2)
    def empty = fa.empty ++ fb.empty
    def keys = fa.keys ++ fb.keys
  }
}

case class FormUi(html: Seq[_root_.scalatags.Text.Modifier]) { lhs =>
  def ++ (rhs: FormUi): FormUi = FormUi(lhs.html ++ rhs.html)
}
