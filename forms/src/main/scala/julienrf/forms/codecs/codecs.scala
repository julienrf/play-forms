package julienrf.forms.codecs

import julienrf.forms.FieldData
import scala.util.control.NonFatal


/**
 * As its name suggests, a `Codec[A, B]`, defines how to '''decode''' an `A` value to a `B` value, and, conversely,
 * how to '''encode''' a `B` value to an `A` value.
 *
 * The decoding and encoding processes are defined by the [[decode]] and [[encode]] methods.
 *
 * To get an instance of `Codec`, start with the provided '''concrete subclasses''' (see the [[Codec$ companion object]]) and '''combine''' them with the
 * [[andThen]], [[orElse]] and [[opt]] methods.
 *
 * @see [[Constraint]]: a specialization of `Codec` that can be seen as a validator of `A` values.
 *
 * @groupname combinators Combinators
 * @groupprio combinators 20
 *
 * @groupname primary Primary Members
 * @groupprio primary 10
 */
// FIXME Separate decode and encode and make B covariant and A contravariant?
sealed abstract class Codec[A, B] {

  /**
   * Attempts to decode a value of type `B` from the specified `A` value.
   *
   * @param a value to decode
   * @return a `Left` value containing the errors if the decoding process failed, otherwise a `Right` value containing the `B`
   * @note in case of failure, the sequence of errors is not empty
   * @group primary
   */
  def decode(a: A): Either[Seq[Throwable], B]

  /**
   * Encodes a value of type `B` to an `A` value.
   *
   * The return type is `Option[A]` rather than `A` because the absence of an `A` value is sometimes the way you want
   * to encode some `B` value.
   *
   * @param b value to encode
   * @return the encoded value
   * @group primary
   */
  def encode(b: B): A

  /**
   * Alias for [[andThen]]
   *
   * @group combinators
   */
  final def >=> [C](that: Codec[B, C]): Codec[A, C] = this andThen that

  /**
   * Chaining combinator.
   *
   * @param that `Codec` to run after `this` `Codec` has run
   * @return a `Codec` that decodes values by first running `this` `Codec` and then the specified `Codec`
   * @group combinators
   */
  final def andThen[C](that: Codec[B, C]): Codec[A, C] = Codec.AndThen(this, that)

  /**
   * Alternative combinator.
   *
   * @param that `Codec` to run if `this` `Codec` fails
   * @return a `Codec` that runs the specified `Codec` if `this` `Codec` failed
   * @group combinators
   */
  final def orElse[C](that: Codec[A, C]): Codec[A, Either[B, C]] = Codec.OrElse(this, that)

  /**
   * Alias for [[orElse]]
   *
   * @group combinators
   */
  final def || [C](that: Codec[A, C]): Codec[A, Either[B, C]] = this orElse that

  /**
   * Alias for [[opt]]
   *
   * @group combinators
   */
  final def ? [C](implicit ev: A <:< Option[C]): Codec[Option[C], Option[B]] = opt[C]

  /**
   * Optional combinator.
   *
   * @return a `Codec` that turns a failure into a successful `None` value
   * @group combinators
   */
  final def opt[C](implicit ev: A <:< Option[C]): Codec[Option[C], Option[B]] =
    Codec.Opt(this.asInstanceOf[Codec[Option[C], B]])

}


/**
 * Lists all the built-in `Codec`s.
 *
 * @groupname field Field Codecs
 * @groupprio field 10
 *
 * @groupname constraint Constraints
 * @groupprio constraint 20
 */
object Codec {

  /**
   * Attempts to get a `String` value from a form field data.
   *
   * @note Empty `String` values are not considered to be valid. Use the [[Codec#opt opt]] combinator to optionally decode a `String`.
   * @group field
   */
  val text: Codec[FieldData, String] = Head

  /**
   * Attempts to get an `Int` from the form field data.
   *
   * @group field
   */
  val int: Codec[FieldData, Int] = Head >=> ToInt

  /**
   * Attempts to get a `Boolean` value form the form field data.
   *
   * Decodes `true` if the field data is not empty, `false` otherwise.
   *
   * @group field
   */
  val boolean: Codec[FieldData, Boolean] = ToBoolean

  /**
   * Attempts to map the form field data to an `A` value using the specified `Map`.
   *
   * @note the values of the `Map` must be unique
   *
   * @group field
   */
  def oneOf[A](valuesToKey: Map[A, String]): Codec[FieldData, A] = Head >=> OneOf(valuesToKey)

  /**
   * Attempts to map tfe form field data to several `A` values using the specified `Map`.
   *
   * @note if one of the values of the form field data is not a valid index in the `Map`, the `Codec` fails
   *
   * @note the values of the `Map` must be unique
   *
   * @group field
   */
  def severalOf[A](valuesToKey: Map[A, String]): Codec[FieldData, Seq[A]] = SeveralOf(valuesToKey)

  //  def partialFunction[A, B](f: PartialFunction[A, B]): A => Result[B] = a => {
//    if (f.isDefinedAt(a)) Success(f(a))
//    else Failure(Seq(Error.Undefined))
//  }

  // --- Combinators

  private[forms] final case class AndThen[A, B, C](codec1: Codec[A, B], codec2: Codec[B, C]) extends Codec[A, C] {

    def decode(a: A): Either[Seq[Throwable], C] = codec1.decode(a).right.flatMap(codec2.decode)

    def encode(c: C): A = codec1.encode(codec2.encode(c))

  }

  private[forms] final case class OrElse[A, B, C](codec1: Codec[A, B], codec2: Codec[A, C]) extends Codec[A, Either[B, C]] {

    def decode(a: A): Either[Seq[Throwable], Either[B, C]] = codec1.decode(a) match {
      case Right(b) => Right(Left(b))
      case Left(_) => codec2.decode(a).right.map(c => Right(c))
    }

    def encode(bOrC: Either[B, C]): A = bOrC match {
      case Left(b) => codec1.encode(b)
      case Right(c) => codec2.encode(c)
    }

  }

  private[forms] final case class Opt[A, B](codec: Codec[Option[A], B]) extends Codec[Option[A], Option[B]] {

    def decode(a: Option[A]): Either[Seq[Throwable], Option[B]] = Right(codec.decode(a).right.toOption)

    def encode(maybeB: Option[B]): Option[A] = maybeB.flatMap(codec.encode)

  }


  // --- Codecs

  private[forms] case object Head extends Codec[FieldData, String] {

    def decode(fieldData: FieldData): Either[Seq[Throwable], String] =
      fieldData.flatMap(_.headOption).filter(_.nonEmpty) match {
        case Some(s) => Right(s)
        case None => Left(Seq(Error.Required))
      }

    def encode(string: String): FieldData = Some(Seq(string))

  }


  private[forms] case object ToInt extends Codec[String, Int] {

    def decode(s: String): Either[Seq[Throwable], Int] = try {
      Right(s.toInt)
    } catch {
      case NonFatal(e) => Left(Seq(e))
    }

    def encode(n: Int): String = n.toString

  }


  private[forms] case object ToBoolean extends Codec[FieldData, Boolean] {

    def decode(data: FieldData): Either[Seq[Throwable], Boolean] = Right(data.nonEmpty)

    def encode(b: Boolean): FieldData = if (b) Some(Nil) else None

  }

//  // FIXME Useful?
//  private[forms] final case class InMap[A, B, C](codec: Codec[A, B], f1: B => C, f2: C => B) extends Codec[A, C] {
//
//    def decode(a: A) = codec.decode(a).right.map(f1)
//
//    def encode(c: C) = codec.encode(f2(c))
//
//  }

  private[forms] final case class OneOf[A](valuesToKey: Map[A, String]) extends Codec[String, A] {

    require (valuesToKey.values.to[Seq].distinct.size == valuesToKey.values.size)

    val keysToValue = valuesToKey map { case (v, k) => k -> v }

    def decode(s: String): Either[Seq[Throwable], A] =
      keysToValue.get(s) match {
        case Some(a) => Right(a)
        case None => Left(Seq(Error.Undefined))
      }

    // Assumes that valuesToKey is exhaustive
    def encode(a: A): String = valuesToKey(a)

  }

  private[forms] final case class SeveralOf[A](valuesToKey: Map[A, String]) extends Codec[FieldData, Seq[A]] {

    require (valuesToKey.values.to[Seq].distinct.size == valuesToKey.values.size)

    val keysToValue = valuesToKey map { case (v, k) => k -> v }

    // If one fails, decoding fails with the first error
    def decode(data: FieldData): Either[Seq[Throwable], Seq[A]] =
      data
        .to[Seq].flatten
        .foldLeft[Either[Seq[Throwable], Seq[A]]](Right(Nil)) { (result, k) =>
          result.right.flatMap { as =>
            keysToValue.get(k) match {
              case Some(a) => Right(as :+ a)
              case None => Left(Seq(Error.Undefined))
            }
          }
        }

    // Assumes that valuesToKey is exhaustive
    def encode(as: Seq[A]): FieldData = Some(as.map(valuesToKey))

  }
}

/**
 * Specialisation of `Codec[A, A]` that can be seen as a validator of `A` values.
 *
 * This class inherits all the combinators of [[Codec]] and defines an additional combinator, [[and]].
 *
 * @note a `Constraint` can '''not''' modify the validated value
 */
sealed abstract class Constraint[A] extends Codec[A, A] {

  /**
   * Apply some validation logic to the specified value
   * @param a value to validate
   * @return `None` if the value is valid, otherwise `Some` sequence of validation errors
   * @group primary
   */
  def validate(a: A): Option[Seq[Throwable]]

  final def decode(a: A): Either[Seq[Throwable], A] = validate(a) match {
    case Some(errors) => Left(errors)
    case None => Right(a)
  }

  final def encode(a: A): A = a

  /**
   * Alias for [[and]]
   *
   * @group combinators
   */
  final def && (that: Constraint[A]): Constraint[A] = this and that

  /**
   * Conjunction combinator.
   *
   * @param that a `Constraint` to apply in addition to `this` `Constraint`
   * @return a `Constraint` that applies `this` and `that` validation rules
   * @group combinators
   */
  final def and(that: Constraint[A]): Constraint[A] = Constraint.And(this, that)

}

object Constraint {

  /**
   * Checks that the decoded value is greater or equal to `n`
   *
   * @group constraint
   */
  def min(n: Int): Constraint[Int] = Min(n)


  // --- Combinators

  private[forms] final case class And[A](constraint1: Constraint[A], constraint2: Constraint[A]) extends Constraint[A] {

    def validate(a: A): Option[Seq[Throwable]] = (constraint1.validate(a), constraint2.validate(a)) match {
      case (None, None) => None
      case (Some(errors), None) => Some(errors)
      case (None, Some(errors)) => Some(errors)
      case (Some(e1s), Some(e2s)) => Some(e1s ++ e2s)
    }

  }


  // --- Constraints

  private[forms] final case class Min(n: Int) extends Constraint[Int] {

    def validate(m: Int): Option[Seq[Throwable]] = if (m >= n) None else Some(Seq(Error.MustBeAtLeast(n)))

  }

}

object Error {
  case object Required extends Throwable
  case class MustBeAtLeast(n: Int) extends Throwable
  case object Undefined extends Throwable
}
