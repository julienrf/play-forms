package julienrf.forms.rules

import julienrf.forms.FormData
import julienrf.forms.rules.Result.ResultOps
import play.api.data.mapping.{Failure, Success}

sealed abstract class Rule[A, B](val run: A => Result[B], val show: B => String) {

  def >=> [C](that: Rule[B, C]): Rule[A, C] = AndThen(this, that)

//  def && [C](that: Rule[A, C]): Rule[A, (B, C)] = And(this, that)

  def || (that: Rule[A, B]): Rule[A, B] = Or(this, that)

  def ? : Rule[A, Option[B]] = Opt(this)

}

case object Head extends Rule[(FormData, String), String]({ case (data, key) =>
  data.get(key).flatMap(_.headOption) match {
    case Some(s) => Success(s)
    case None => Failure(Seq(Error.Required))
  }
}, identity)

case class AndThen[A, B, C](rule1: Rule[A, B], rule2: Rule[B, C]) extends Rule[A, C](a => rule1.run(a).flatMap(rule2.run), rule2.show)
//case class And[A, B, C](rule1: Rule[A, B], rule2: Rule[A, C]) extends Rule[A, (B, C)](a => (rule1.run(a) and rule2.run(a)).tupled, rule2.unbind)
case class Or[A, B](rule1: Rule[A, B], rule2: Rule[A, B]) extends Rule[A, B](a => rule1.run(a) orElse rule2.run(a), rule2.show)

case object ToInt extends Rule[String, Int](s => Result.fromTryCatch(s.toInt), _.toString)
case class Min(n: Int) extends Rule[Int, Int](a => if (a >= n) Success(a) else Failure(Seq(Error.MustBeAtLeast(n))), _.toString)
case class Opt[A, B](rule: Rule[A, B]) extends Rule[A, Option[B]](a => Success(rule.run(a).toOption), { case Some(b) => rule.show(b); case None => "" })
case class InMap[A, B](rule: Rule[FormData, A], f1: A => B, f2: B => A) extends Rule[FormData, B](d => rule.run(d).map(f1), f2 andThen rule.show)

object UsualRules {
  val text: Rule[(FormData, String), String] = Head
  val int: Rule[(FormData, String), Int] = Head >=> ToInt
//  def and[A, B, C](lhs: Rule[A, B], rhs: Rule[A, C]): Rule[A, (B, C)] = And(lhs, rhs)
  def min(n: Int): Rule[Int, Int] = Min(n)
}

object Error {
  case object Required extends Throwable
  case class MustBeAtLeast(n: Int) extends Throwable
}
