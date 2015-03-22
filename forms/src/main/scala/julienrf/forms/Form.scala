package julienrf.forms

import julienrf.forms.presenters.{Field, Presenter}
import julienrf.forms.codecs.Codec
import play.api.libs.functional.{FunctionalCanBuild, InvariantFunctor, ~}
import play.api.mvc.{BodyParsers, BodyParser, Result}

import scala.concurrent.{ExecutionContext, Future}

// FIXME abstract over FormUi (we just need a SemiGroup)
sealed trait Form[A] {
  def bind(data: FormData): Either[FormUi, A]
  def unbind(a: A): FormUi
  def empty: FormUi
  def keys: Seq[String]

  def bodyParser(errorHandler: FormUi => Future[Result])(implicit ec: ExecutionContext): BodyParser[A] = BodyParsers.parse.urlFormEncoded.validateM(data => bind(data) match {
    case Left(errors) => errorHandler(errors).map(Left(_))
    case Right(a) => Future.successful(Right(a))
  })
}

final case class FieldForm[A](field: (String, Codec[FieldData, A]), presenter: Presenter[A]) extends Form[A] {
  val (name, codec) = field
  def bind(data: FormData) = {
    val value = data.getOrElse(name, Nil)
    codec.decode(value)
      .left.map(errors => presenter.render(Field(name, codec, value, errors)))
  }
  def unbind(a: A) = presenter.render(Field(name, codec, codec.encode(a) getOrElse Nil, Nil))
  def empty = presenter.render(Field(name, codec, Nil, Nil))
  def keys = Seq(name)
}

object Form {

  def field[A](name: String, codec: Codec[FieldData, A])(presenter: Presenter[A]): Form[A] =
    FieldForm((name, codec), presenter)

  def form[A](key: String, fa: Form[A]): Form[A] =
    fa match {
      case FieldForm((subKey, codec), presenter) => FieldForm((s"$key.$subKey", codec), presenter)
      case Form.InMap(fa, f1, f2) => Form.InMap(form(key, fa), f1, f2)
      case Form.Apply(fa, fb) => Form.Apply(form(key, fa), form(key, fb))
    }

  /**
   * TODO
   */
  implicit val formInstances: InvariantFunctor[Form] with FunctionalCanBuild[Form] =
    new InvariantFunctor[Form] with FunctionalCanBuild[Form] {
      def inmap[A, B](fa: Form[A], f1: A => B, f2: B => A) = InMap(fa, f1, f2)
      def apply[A, B](fa: Form[A], fb: Form[B]) = Apply(fa, fb)
    }

  final case class InMap[A, B](fa: Form[A], f1: A => B, f2: B => A) extends Form[B] {
    def bind(data: FormData) = fa.bind(data) match {
      case Left(errors) => Left(errors)
      case Right(a) => Right(f1(a))
    }
    def unbind(b: B) = fa.unbind(f2(b))
    def empty = fa.empty
    def keys = fa.keys
  }

  final case class Apply[A, B](fa: Form[A], fb: Form[B]) extends Form[A ~ B] {
    require((fa.keys intersect fb.keys).isEmpty) // You can not have two fields with the same path
    def bind(data: FormData) = (fa.bind(data), fb.bind(data)) match {
      case (Right(a), Right(b)) => Right(new ~(a, b))
      case (Right(a), Left(es)) => Left(fa.unbind(a) ++ es)
      case (Left(es), Right(b)) => Left(es ++ fb.unbind(b))
      case (Left(es1), Left(es2)) => Left(es1 ++ es2)
    }
    def unbind(ab: A ~ B) = fa.unbind(ab._1) ++ fb.unbind(ab._2)
    def empty = fa.empty ++ fb.empty
    def keys = fa.keys ++ fb.keys
  }
}

case class FormUi(html: Seq[_root_.scalatags.Text.Modifier]) { lhs =>
  def ++ (rhs: FormUi): FormUi = FormUi(lhs.html ++ rhs.html)
}
