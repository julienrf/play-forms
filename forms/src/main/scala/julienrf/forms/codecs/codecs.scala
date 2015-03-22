package julienrf.forms.codecs

import julienrf.forms.FieldData
import scala.util.control.NonFatal


// FIXME Separate decode and encode and make B covariant and A contravariant?
sealed abstract class Codec[A, B] {

  def decode(a: A): Either[Seq[Throwable], B]

  def encode(b: B): Option[A]

  final def >=> [C](that: Codec[B, C]): Codec[A, C] = this andThen that

  final def andThen[C](that: Codec[B, C]): Codec[A, C] = AndThen(this, that)

  final def orElse[C](that: Codec[A, C]): Codec[A, Either[B, C]] = OrElse(this, that)

  final def || [C](that: Codec[A, C]): Codec[A, Either[B, C]] = this orElse that

  final def ? : Codec[A, Option[B]] = opt

  final def opt: Codec[A, Option[B]] = Opt(this)

}


sealed abstract class Constraint[A] extends Codec[A, A] {

  def validate(a: A): Option[Seq[Throwable]]

  final def decode(a: A) = validate(a) match {
    case Some(errors) => Left(errors)
    case None => Right(a)
  }

  final def encode(a: A) = Some(a)

  final def &&(that: Constraint[A]): Constraint[A] = And(this, that)

}


case object Head extends Codec[FieldData, String] {

  def decode(fieldData: FieldData) =
    fieldData.headOption.filter(_.nonEmpty) match {
      case Some(s) => Right(s)
      case None => Left(Seq(Error.Required))
    }

  def encode(string: String) = Some(Seq(string))

}


final case class AndThen[A, B, C](rule1: Codec[A, B], rule2: Codec[B, C]) extends Codec[A, C] {

  def decode(a: A) = rule1.decode(a).right.flatMap(rule2.decode)

  def encode(c: C) = rule2.encode(c).flatMap(rule1.encode)

}


final case class And[A](constraint1: Constraint[A], constraint2: Constraint[A]) extends Constraint[A] {

  def validate(a: A) = (constraint1.validate(a), constraint2.validate(a)) match {
    case (None, None) => None
    case (Some(errors), None) => Some(errors)
    case (None, Some(errors)) => Some(errors)
    case (Some(e1s), Some(e2s)) => Some(e1s ++ e2s)
  }

}


final case class OrElse[A, B, C](rule1: Codec[A, B], rule2: Codec[A, C]) extends Codec[A, Either[B, C]] {

  def decode(a: A) = rule1.decode(a) match {
    case Right(b) => Right(Left(b))
    case Left(_) => rule2.decode(a).right.map(c => Right(c))
  }

  def encode(bOrC: Either[B, C]) = bOrC match {
    case Left(b) => rule1.encode(b)
    case Right(c) => rule2.encode(c)
  }

}


case object ToInt extends Codec[String, Int] {

  def decode(s: String) = try {
    Right(s.toInt)
  } catch {
    case NonFatal(e) => Left(Seq(e))
  }

  def encode(n: Int) = Some(n.toString)

}


final case class Min(n: Int) extends Constraint[Int] {

  def validate(m: Int) = if (m >= n) None else Some(Seq(Error.MustBeAtLeast(n)))

}


final case class Opt[A, B](codec: Codec[A, B]) extends Codec[A, Option[B]] {

  def decode(a: A) = Right(codec.decode(a).right.toOption)

  def encode(maybeB: Option[B]) = maybeB.flatMap(codec.encode)

}

// FIXME Useful?
final case class InMap[A, B, C](codec: Codec[A, B], f1: B => C, f2: C => B) extends Codec[A, C] {

  def decode(a: A) = codec.decode(a).right.map(f1)

  def encode(c: C) = codec.encode(f2(c))

}


final case class OneOf[A](valuesToKey: Map[A, String]) extends Codec[String, A] {

  require (valuesToKey.values.to[Seq].distinct.size == valuesToKey.values.size)

  val keysToValue = valuesToKey map { case (v, k) => k -> v }

  def decode(s: String) =
    keysToValue.get(s) match {
      case Some(a) => Right(a)
      case None => Left(Seq(Error.Undefined))
    }

  // Assumes that valuesToKey is exhaustive
  def encode(a: A) = Some(valuesToKey(a))

}

final case class SeveralOf[A](valuesToKey: Map[A, String]) extends Codec[FieldData, Seq[A]] {

  require (valuesToKey.values.to[Seq].distinct.size == valuesToKey.values.size)

  val keysToValue = valuesToKey map { case (v, k) => k -> v }

  // If one fails, decoding fails with the first error
  def decode(data: FieldData) =
    data.foldLeft[Either[Seq[Throwable], Seq[A]]](Right(Nil)) { (result, k) =>
      result.right.flatMap { as =>
        keysToValue.get(k) match {
          case Some(a) => Right(as :+ a)
          case None => Left(Seq(Error.Undefined))
        }
      }
    }

  // Assumes that valuesToKey is exhaustive
  def encode(as: Seq[A]) = Some(as.map(valuesToKey))

}


object Codec {

  val text: Codec[FieldData, String] = Head

  val int: Codec[FieldData, Int] = Head >=> ToInt

  def min(n: Int): Codec[Int, Int] = Min(n)

  def oneOf[A](valuesToKey: Map[A, String]): Codec[FieldData, A] = Head >=> OneOf(valuesToKey)

  def severalOf[A](valuesToKey: Map[A, String]): Codec[FieldData, Seq[A]] = SeveralOf(valuesToKey)

//  def partialFunction[A, B](f: PartialFunction[A, B]): A => Result[B] = a => {
//    if (f.isDefinedAt(a)) Success(f(a))
//    else Failure(Seq(Error.Undefined))
//  }

}

object Error {
  case object Required extends Throwable
  case class MustBeAtLeast(n: Int) extends Throwable
  case object Undefined extends Throwable
}
