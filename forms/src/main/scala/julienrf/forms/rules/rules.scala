package julienrf.forms.rules

import play.api.data.mapping.{Failure, Success}
import play.api.libs.functional.syntax._
import Result.{ResultOps, functor}

sealed abstract class Rule[A, B](val run: A => Result[B]) {

  def >>> [C](that: Rule[B, C]): Rule[A, C] = AndThen(this, that)

  def && [C](that: Rule[A, C]): Rule[A, (B, C)] = And(this, that)

  def || (that: Rule[A, B]): Rule[A, B] = Or(this, that)

  def ? : Rule[A, Option[B]] = Opt(this)

}

case object Head extends Rule[InputData, String]({ case (data, key) =>
  data.get(key).flatMap(_.headOption) match {
    case Some(s) => Success(s)
    case None => Failure(Seq(Error.Required))
  }
})

case class AndThen[A, B, C](rule1: Rule[A, B], rule2: Rule[B, C]) extends Rule[A, C](a => rule1.run(a).flatMap(rule2.run))
case class And[A, B, C](rule1: Rule[A, B], rule2: Rule[A, C]) extends Rule[A, (B, C)](a => (rule1.run(a) and rule2.run(a)).tupled)
case class Or[A, B](rule1: Rule[A, B], rule2: Rule[A, B]) extends Rule[A, B](a => rule1.run(a) orElse rule2.run(a))
case class Id[A]() extends Rule[A, A](a => Success(a))

case object ToInt extends Rule[String, Int](s => Result.fromTryCatch(s.toInt))
case class Min(n: Int) extends Rule[Int, Int](a => if (a >= n) Success(a) else Failure(Seq(Error.MustBeAtLeast(n))))
case class Opt[A, B](rule: Rule[A, B]) extends Rule[A, Option[B]](a => Success(rule.run(a).toOption))

object UsualRules {
  val text: Rule[InputData, String] = Head
  val int: Rule[InputData, Int] = Head >>> ToInt
  def opt[A, B](rule: Rule[A, B]): Rule[A, Option[B]] = Opt(rule)
  def and[A, B, C](lhs: Rule[A, B], rhs: Rule[A, C]): Rule[A, (B, C)] = And(lhs, rhs)
  def or[A, B](lhs: Rule[A, B], rhs: Rule[A, B]): Rule[A, B] = Or(lhs, rhs)
  def min(n: Int): Rule[Int, Int] = Min(n)
  def id[A]: Rule[A, A] = Id[A]()
}

object Error {
  case object Required extends Throwable
  case class MustBeAtLeast(n: Int) extends Throwable
}
