package julienrf.forms.presenters

case class InputType[A](tpe: String)

object InputType {
  implicit val inputString: InputType[String] = InputType[String]("text")
  implicit val inputInt: InputType[Int] = InputType[Int]("number")
  implicit val inputDouble: InputType[Double] = InputType[Double]("number")
  implicit def inputOption[A](implicit A: InputType[A]): InputType[Option[A]] = InputType[Option[A]](A.tpe)

  @inline def apply[A : InputType]: InputType[A] = implicitly[InputType[A]]
}

case class Mandatory[A](value: Boolean)

object Mandatory {
  implicit def mandatoryOption[A]: Mandatory[Option[A]] = Mandatory[Option[A]](value = false)
  implicit def mandatory[A]: Mandatory[A] = Mandatory[A](value = true)

  @inline def apply[A : Mandatory]: Mandatory[A] = implicitly[Mandatory[A]]
}
