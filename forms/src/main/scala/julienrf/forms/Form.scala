package julienrf.forms

import julienrf.forms.rules.Rule
import julienrf.forms.presenters.Presenter
import play.api.libs.functional.{FunctionalCanBuild, InvariantFunctor, ~}
import play.api.mvc.{BodyParsers, BodyParser, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

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

final case class FieldForm[A](field: (String, Rule[(FormData, String), A]), presenter: Presenter[A]) extends Form[A] {
  val (name, rule) = field
  val f = presenter.field(name, rule)
  def bind(data: FormData) = rule.run((data, name)) match {
    case Success(a) => Right(a)
    case Failure(error) => Left(presenter render f.addingError(error))
  }
  def unbind(a: A) = presenter render f.withValue(rule.show(a))
  def empty = presenter render f
  def keys = Seq(name)
}

object Form {

  def field[A](name: String, rule: Rule[(FormData, String), A])(presenter: Presenter[A]): Form[A] =
    FieldForm((name, rule), presenter)

  def form[A](key: String, fa: Form[A]): Form[A] =
    fa match {
      case FieldForm((subKey, rule), presenter) => FieldForm((s"$key.$subKey", rule), presenter)
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

case class FormUi(html: Seq[scalatags.Text.Modifier]) { lhs =>
  def ++ (rhs: FormUi): FormUi = FormUi(lhs.html ++ rhs.html)
}
