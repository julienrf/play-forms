package julienrf.forms.rules

import play.api.data.mapping.{Failure, Success}

import scala.util.control.NonFatal

object Result {

  def fromTryCatch[A](a: => A): Result[A] =
    try { Success(a) } catch { case NonFatal(t) => Failure(Seq(t)) }

  implicit class ResultOps[A](ra: Result[A]) {
    def toOption: Option[A] = ra match {
      case Success(a) => Some(a)
      case Failure(_) => None
    }

    def flatMap[B](f: A => Result[B]): Result[B] = ra match {
      case Success(a) => f(a)
      case Failure(e) => Failure(e)
    }
  }
  
  
}