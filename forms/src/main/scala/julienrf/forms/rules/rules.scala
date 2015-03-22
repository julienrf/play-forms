package julienrf.forms.rules

import julienrf.forms.FieldData

import scala.util.{Failure, Success, Try}

// FIXME Separate decode and encode and make B covariant and A contravariant?
// TODO `run: A => Either[Seq[Throwable], B]`
sealed abstract class Rule[A, B](val decode: A => Try[B], val encode: B => Option[A]) {

  final def >=> [C](that: Rule[B, C]): Rule[A, C] = this andThen that

  final def andThen[C](that: Rule[B, C]): Rule[A, C] = AndThen(this, that)

//  def && (that: Rule[A, A])(implicit ev: A <:< B): Rule[A, A] = And(this, that)

  final def || (that: Rule[A, B]): Rule[A, B] = Or(this, that)

  final def ? : Rule[A, Option[B]] = opt

  final def opt: Rule[A, Option[B]] = Opt(this)

}

// FIXME Useful?
sealed abstract class Constraint[A](decode: A => Try[A]) extends Rule[A, A](decode, Some(_))

case object Head extends Rule[FieldData, String]({ data =>
  data.headOption.filter(_.nonEmpty) match {
    case Some(s) => Success(s)
    case None => Failure(Error.Required)
  }
}, s => Some(Seq(s)))

case class AndThen[A, B, C](rule1: Rule[A, B], rule2: Rule[B, C]) extends Rule[A, C](a => rule1.decode(a).flatMap(rule2.decode), c => rule2.encode(c).flatMap(rule1.encode))
//case class And[A, B, C](rule1: Rule[A, B], rule2: Rule[A, C]) extends Rule[A, (B, C)](a => (rule1.run(a) and rule2.run(a)).tupled, rule2.unbind)
case class Or[A, B](rule1: Rule[A, B], rule2: Rule[A, B]) extends Rule[A, B](a => rule1.decode(a) orElse rule2.decode(a), rule2.encode)

case object ToInt extends Rule[String, Int](s => Try(s.toInt), n => Some(n.toString))
case class Min(n: Int) extends Constraint[Int](a => if (a >= n) Success(a) else Failure(Error.MustBeAtLeast(n)))
case class Opt[A, B](rule: Rule[A, B]) extends Rule[A, Option[B]](a => Success(rule.decode(a).toOption), _.flatMap(rule.encode))
case class InMap[A, B](rule: Rule[FieldData, A], f1: A => B, f2: B => A) extends Rule[FieldData, B](d => rule.decode(d).map(f1), f2 andThen rule.encode)
case class OneOf[A](valuesToKey: Map[A, String]) extends Rule[String, A]({
  val keysToValue = valuesToKey map { case (v, k) => k -> v }
  keysToValue.get _ andThen {
    case Some(a) => Success(a)
    case None => Failure(Error.Undefined)
  }
}, valuesToKey.get) {
  require (valuesToKey.values.to[Seq].distinct.size == valuesToKey.values.size)
}

object Rule {
  val text: Rule[FieldData, String] = Head
  val int: Rule[FieldData, Int] = Head >=> ToInt
//  def and[A, B, C](lhs: Rule[A, B], rhs: Rule[A, C]): Rule[A, (B, C)] = And(lhs, rhs)
  def min(n: Int): Rule[Int, Int] = Min(n)

//  def partialFunction[A, B](f: PartialFunction[A, B]): A => Result[B] = a => {
//    if (f.isDefinedAt(a)) Success(f(a))
//    else Failure(Seq(Error.Undefined))
//  }

  def oneOf[A](valuesToKey: Map[A, String]): Rule[FieldData, A] = Head >=> OneOf(valuesToKey)

}

object Error {
  case object Required extends Throwable
  case class MustBeAtLeast(n: Int) extends Throwable
  case object Undefined extends Throwable
}
