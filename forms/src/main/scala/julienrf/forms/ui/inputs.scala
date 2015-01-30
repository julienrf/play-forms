package julienrf.forms.ui

case class InputType[A](tpe: String)
object InputType {
  implicit val inputString: InputType[String] = InputType[String]("text")
  implicit val inputInt: InputType[Int] = InputType[Int]("number")
  implicit val inputDouble: InputType[Double] = InputType[Double]("number")
  implicit def inputOption[A](implicit A: InputType[A]): InputType[Option[A]] = InputType[Option[A]](A.tpe)
}

case class Mandatory[A](value: Boolean)
trait MandatoryInstances {

}
object Mandatory {
  implicit def mandatoryOption[A]: Mandatory[Option[A]] = Mandatory[Option[A]](value = false)
  implicit def mandatory[A]: Mandatory[A] = Mandatory[A](value = true)
}

class InputAttrs[A : InputType : Mandatory] {
  val InputType = implicitly[InputType[A]]
  val Mandatory = implicitly[Mandatory[A]]
}

object InputAttrs {
  implicit def get[A : InputType : Mandatory]: InputAttrs[A] = new InputAttrs[A]
  def apply[A : InputAttrs]: InputAttrs[A] = implicitly[InputAttrs[A]]
}
