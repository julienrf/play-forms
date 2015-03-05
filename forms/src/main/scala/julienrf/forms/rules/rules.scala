package julienrf.forms.rules

import julienrf.forms.FormData

import scala.util.{Failure, Success, Try}

// FIXME Separate run and show and make B covariant and A contravariant?
sealed abstract class Rule[A, B](val run: A => Try[B], val show: B => String) {

  final def >=> [C](that: Rule[B, C]): Rule[A, C] = AndThen(this, that)

  final def andThen[C](that: Rule[B, C]): Rule[A, C] = this >=> that

//  def && [C](that: Rule[A, C]): Rule[A, (B, C)] = And(this, that)

  final def || (that: Rule[A, B]): Rule[A, B] = Or(this, that)

  final def ? : Rule[A, Option[B]] = Opt(this)

  final def opt: Rule[A, Option[B]] = ?

}

case object Head extends Rule[(FormData, String), String]({ case (data, key) =>
  data.get(key).flatMap(_.headOption).filter(_.nonEmpty) match {
    case Some(s) => Success(s)
    case None => Failure(Error.Required)
  }
}, identity)

case class AndThen[A, B, C](rule1: Rule[A, B], rule2: Rule[B, C]) extends Rule[A, C](a => rule1.run(a).flatMap(rule2.run), rule2.show)
//case class And[A, B, C](rule1: Rule[A, B], rule2: Rule[A, C]) extends Rule[A, (B, C)](a => (rule1.run(a) and rule2.run(a)).tupled, rule2.unbind)
case class Or[A, B](rule1: Rule[A, B], rule2: Rule[A, B]) extends Rule[A, B](a => rule1.run(a) orElse rule2.run(a), rule2.show)

case object ToInt extends Rule[String, Int](s => Try(s.toInt), _.toString)
case class Min(n: Int) extends Rule[Int, Int](a => if (a >= n) Success(a) else Failure(Error.MustBeAtLeast(n)), _.toString)
case class Opt[A, B](rule: Rule[A, B]) extends Rule[A, Option[B]](a => Success(rule.run(a).toOption), { case Some(b) => rule.show(b); case None => "" })
case class InMap[A, B](rule: Rule[FormData, A], f1: A => B, f2: B => A) extends Rule[FormData, B](d => rule.run(d).map(f1), f2 andThen rule.show)
case class OneOf[A](valuesToKey: Map[A, String]) extends Rule[String, A]({
  val keysToValue = (valuesToKey map { case (v, k) => k -> v }).toMap
  keysToValue.get _ andThen {
    case Some(a) => Success(a)
    case None => Failure(Error.Undefined)
  }
}, valuesToKey.apply) {
  require (valuesToKey.values.to[Seq].distinct.size == valuesToKey.values.size)
}

object UsualRules {
  val text: Rule[(FormData, String), String] = Head
  val int: Rule[(FormData, String), Int] = Head >=> ToInt
//  def and[A, B, C](lhs: Rule[A, B], rhs: Rule[A, C]): Rule[A, (B, C)] = And(lhs, rhs)
  def min(n: Int): Rule[Int, Int] = Min(n)

//  def partialFunction[A, B](f: PartialFunction[A, B]): A => Result[B] = a => {
//    if (f.isDefinedAt(a)) Success(f(a))
//    else Failure(Seq(Error.Undefined))
//  }

  def oneOf[A](valuesToKey: Map[A, String]): Rule[(FormData, String), A] = Head >=> OneOf(valuesToKey)

}

object Error {
  case object Required extends Throwable
  case class MustBeAtLeast(n: Int) extends Throwable
  case object Undefined extends Throwable
}
