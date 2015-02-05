package julienrf.forms.rules

import play.api.data.mapping.{Failure, Success}
import Result.ResultOps

class Rule[A, B](val run: A => Result[B]) {
  def >>> [C](that: Rule[B, C]): Rule[A, C] = AndThen(this, that)
}

case class AndThen[A, B, C](rule1: Rule[A, B], rule2: Rule[B, C]) extends Rule[A, C](a => rule1.run(a).flatMap(rule2.run))
case class Id[A]() extends Rule[A, A](a => Success(a))

case object ToInt extends Rule[String, Int](s => Result.fromTryCatch(s.toInt))
case class Min(n: Int) extends Rule[Int, Int](a => if (a >= n) Success(a) else Failure(Seq(Error.MustBeAtLeast(n))))
case class Opt[A, B](rule: Rule[A, B]) extends Rule[A, Option[B]](a => Success(rule.run(a).toOption))

object UsualRules {
  val text: Rule[String, String] = Id[String]()
  val int: Rule[String, Int] = ToInt
  def opt[A, B](rule: Rule[A, B]): Rule[A, Option[B]] = Opt(rule)
  def min(n: Int): Rule[Int, Int] = Min(n)
  def id[A]: Rule[A, A] = Id[A]()
}

object Error {
  case object Required extends Throwable
  case class MustBeAtLeast(n: Int) extends Throwable
}
