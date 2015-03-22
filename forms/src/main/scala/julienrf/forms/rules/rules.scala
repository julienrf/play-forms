package julienrf.forms.rules

import julienrf.forms.FieldData

import scala.util.control.NonFatal

// FIXME Separate decode and encode and make B covariant and A contravariant?
sealed abstract class Rule[A, B](val decode: A => Either[Seq[Throwable], B], val encode: B => Option[A]) {

  final def >=> [C](that: Rule[B, C]): Rule[A, C] = this andThen that

  final def andThen[C](that: Rule[B, C]): Rule[A, C] = AndThen(this, that)

//  final def && [C](that: Rule[A, C]): Rule[A, (B, C)] = And(this, that)

//  final def || [C](that: Rule[A, C]): Rule[A, Either[B, C]] = Or(this, that)

  final def ? : Rule[A, Option[B]] = opt

  final def opt: Rule[A, Option[B]] = Opt(this)

}

// FIXME Useful?
sealed abstract class Constraint[A](p: A => Option[Seq[Throwable]]) extends Rule[A, A](a => p(a) match {
  case Some(errors) => Left(errors)
  case None => Right(a)
}, Some(_))

case object Head extends Rule[FieldData, String]({ data =>
  data.headOption.filter(_.nonEmpty) match {
    case Some(s) => Right(s)
    case None => Left(Seq(Error.Required))
  }
}, s => Some(Seq(s)))

case class AndThen[A, B, C](rule1: Rule[A, B], rule2: Rule[B, C]) extends Rule[A, C](a => rule1.decode(a).right.flatMap(rule2.decode), c => rule2.encode(c).flatMap(rule1.encode))
//case class And[A, B, C](rule1: Rule[A, B], rule2: Rule[A, C]) extends Rule[A, (B, C)](a => (rule1.run(a) and rule2.run(a)).tupled, rule2.unbind)
//case class Or[A, B, C](rule1: Rule[A, B], rule2: Rule[A, C]) extends Rule[A, Either[B, C]](a => rule1.decode(a).left.flatMap(_ => rule2.decode(a)), bOrC match { case Left(b) => rule1.encode(b) case Right(c) => rule2.encode(c) })

case object ToInt extends Rule[String, Int](s => try { Right(s.toInt) } catch { case NonFatal(e) => Left(Seq(e)) }, n => Some(n.toString))
case class Min(n: Int) extends Constraint[Int](a => if (a >= n) None else Some(Seq(Error.MustBeAtLeast(n))))
case class Opt[A, B](rule: Rule[A, B]) extends Rule[A, Option[B]](a => Right(rule.decode(a).right.toOption), _.flatMap(rule.encode))
case class InMap[A, B](rule: Rule[FieldData, A], f1: A => B, f2: B => A) extends Rule[FieldData, B](d => rule.decode(d).right.map(f1), f2 andThen rule.encode)
case class OneOf[A](valuesToKey: Map[A, String]) extends Rule[String, A]({
  val keysToValue = valuesToKey map { case (v, k) => k -> v }
  keysToValue.get _ andThen {
    case Some(a) => Right(a)
    case None => Left(Seq(Error.Undefined))
  }
}, valuesToKey.get) {
  require (valuesToKey.values.to[Seq].distinct.size == valuesToKey.values.size)
}

object Rule {
  val text: Rule[FieldData, String] = Head
  val int: Rule[FieldData, Int] = Head >=> ToInt
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
