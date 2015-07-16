package julienrf.forms

import java.time.LocalDate

import scala.language.higherKinds


case class InputType[A](tpe: String)

object InputType {

  implicit val inputString: InputType[String] = InputType[String]("text")
  implicit val inputInt: InputType[Int] = InputType[Int]("number")
  implicit val inputDouble: InputType[Double] = InputType[Double]("number")
  implicit val inputBigDecimal: InputType[BigDecimal] = InputType[BigDecimal]("number")
  implicit val inputLocalDate: InputType[LocalDate] = InputType[LocalDate]("date")
  implicit val inputFile: InputType[java.io.File] = InputType[java.io.File]("file")
  implicit def inputOption[A](implicit A: InputType[A]): InputType[Option[A]] = InputType[Option[A]](A.tpe)

  @inline def apply[A : InputType]: InputType[A] = implicitly[InputType[A]]
}


case class Mandatory[A](value: Boolean)

object Mandatory {

  implicit def mandatoryOption[A]: Mandatory[Option[A]] = Mandatory[Option[A]](value = false)
  implicit def mandatory[A]: Mandatory[A] = Mandatory[A](value = true) // TODO Remove

  @inline def apply[A : Mandatory]: Mandatory[A] = implicitly[Mandatory[A]]
}

// FIXME Traverse codecs to get this information?
case class Multiple[A](value: Boolean)

object Multiple {

  implicit def multipleCollection[F[a] <: Traversable[a], A]: Multiple[F[A]] = Multiple[F[A]](value = true)
  implicit def multiple[A]: Multiple[A] = Multiple[A](value = false)

  @inline def apply[A : Multiple]: Multiple[A] = implicitly[Multiple[A]]
}