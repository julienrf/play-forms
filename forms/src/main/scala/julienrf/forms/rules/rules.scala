package julienrf.forms.rules

import julienrf.forms.FieldData

import scala.util.control.NonFatal

// FIXME Separate decode and encode and make B covariant and A contravariant?
sealed abstract class Rule[A, B](val decode: A => Either[Seq[Throwable], B], val encode: B => Option[A]) {

  final def >=> [C](that: Rule[B, C]): Rule[A, C] = this andThen that

  final def andThen[C](that: Rule[B, C]): Rule[A, C] = AndThen(this, that)

  final def orElse[C](that: Rule[A, C]): Rule[A, Either[B, C]] = OrElse(this, that)

  final def || [C](that: Rule[A, C]): Rule[A, Either[B, C]] = this orElse that

  final def ? : Rule[A, Option[B]] = opt

  final def opt: Rule[A, Option[B]] = Opt(this)

}

sealed abstract class Constraint[A](val p: A => Option[Seq[Throwable]]) extends Rule[A, A](a => p(a) match {
  case Some(errors) => Left(errors)
  case None => Right(a)
}, Some(_)) {

  final def && (that: Constraint[A]): Constraint[A] = And(this, that)

}

case object Head extends Rule[FieldData, String]({ data =>
  data.headOption.filter(_.nonEmpty) match {
    case Some(s) => Right(s)
    case None => Left(Seq(Error.Required))
  }
}, s => Some(Seq(s)))

case class AndThen[A, B, C](rule1: Rule[A, B], rule2: Rule[B, C]) extends Rule[A, C](a => rule1.decode(a).right.flatMap(rule2.decode), c => rule2.encode(c).flatMap(rule1.encode))

case class And[A, B, C](constraint1: Constraint[A], constraint2: Constraint[A]) extends Constraint[A](a => (constraint1.p(a), constraint2.p(a)) match {
  case (None, None) => None
  case (Some(errors), None) => Some(errors)
  case (None, Some(errors)) => Some(errors)
  case (Some(e1s), Some(e2s)) => Some(e1s ++ e2s)
})

case class OrElse[A, B, C](rule1: Rule[A, B], rule2: Rule[A, C]) extends Rule[A, Either[B, C]](a => {
  rule1.decode(a) match {
    case Right(b) => Right(Left(b))
    case Left(_) => rule2.decode(a).right.map(c => Right(c))
  }
}, { case Left(b) => rule1.encode(b) case Right(c) => rule2.encode(c) })

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
