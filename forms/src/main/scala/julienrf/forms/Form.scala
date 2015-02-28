package julienrf.forms

import julienrf.forms.rules.Rule
import julienrf.forms.ui.{Mandatory, Field, InputType}
import play.api.data.mapping.{Failure, Success}
import play.api.libs.functional.{FunctionalCanBuild, InvariantFunctor, ~}

// FIXME abstract over Fields (we just need a SemiGroup)
trait Form[A] {
  def bind(data: FormData): Either[Fields, A]
  def unbind(a: A): Fields
  def empty: Fields
}

object Form {

  /**
   * TODO
   */
  implicit val formInstances: InvariantFunctor[Form] with FunctionalCanBuild[Form] =
    new InvariantFunctor[Form] with FunctionalCanBuild[Form] {

      def inmap[A, B](fa: Form[A], f1: A => B, f2: B => A) = new Form[B] {

        def bind(data: FormData) = fa.bind(data) match {
          case Left(errors) => Left(errors)
          case Right(a) => Right(f1(a))
        }

        def unbind(b: B) = fa.unbind(f2(b))

        def empty = fa.empty
      }

      def apply[A, B](fa: Form[A], fb: Form[B]) = new Form[A ~ B] {
//        require(fa.empty.map(_.path).intersect(fb.empty.map(_.path)).isEmpty) // You can not have two fields with the same path

        def bind(data: FormData) = (fa.bind(data), fb.bind(data)) match {
          case (Right(a), Right(b)) => Right(new ~(a, b))
          case (Right(a), Left(es)) => Left(fa.unbind(a) ++ es)
          case (Left(es), Right(b)) => Left(es ++ fb.unbind(b))
          case (Left(es1), Left(es2)) => Left(es1 ++ es2)
        }

        def unbind(ab: A ~ B) = fa.unbind(ab._1) ++ fb.unbind(ab._2)

        def empty = fa.empty ++ fb.empty
      }
    }

  def apply[A : InputType : Mandatory](name: String, rule: Rule[(FormData, String), A], f: Field => Fields): Form[A] = new Form[A] {
    val unit = Field(name, rule)
    def bind(data: FormData) = rule.run((data, name)) match {
      case Success(a) => Right(a)
      case Failure(errors) => Left(f(unit.copy(errors = unit.errors ++ errors)))
    }
    def unbind(a: A) = f(unit.copy(value = rule.show(a)))
    def empty = f(unit)
  }

}

case class Fields(html: Seq[scalatags.Text.Modifier]) { lhs =>
  def ++ (rhs: Fields): Fields = Fields(lhs.html ++ rhs.html)
}
