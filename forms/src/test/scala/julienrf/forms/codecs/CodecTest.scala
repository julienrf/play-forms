package julienrf.forms.codecs

import org.scalacheck.{Prop, Properties}
import org.scalacheck.Prop._

object CodecTest extends Properties("Codec") {

  val text = forAll { (s: String) =>
    s.nonEmpty ==> {
      val rule = Codec.text
      succeeds(s, rule.decode(Seq(s))) &&
      rule.decode(Nil) == Left(Seq(Error.Required)) &&
      rule.decode(Seq("")) == Left(Seq(Error.Required))
    }
  }
  val int = forAll { (n: Int) =>
    val rule = Codec.int
    rule.encode(n).exists(data => rule.decode(data) == Right(n)) &&
    rule.decode(Nil) == Left(Seq(Error.Required))
  }
  val min = forAll { (n: Int, m: Int) =>
    val rule = Codec.min(m)
    val result = rule.decode(n)
    if (n >= m) succeeds(n, result) else result == Left(Seq(Error.MustBeAtLeast(m))): Prop
  }

  property("usual rules") = text && int && min

  val kleisli = undecided
  val or = undecided
  val opt = {
    // FIXME I’d like to write forAll { (rule: Rule[A, B], a: A, b: B) => ... }
    val rule = Codec.min(42)
    rule.decode(0).isLeft && succeeds(None, rule.?.decode(0)) &&
    succeeds(42, rule.decode(42)) && succeeds(Some(42), rule.?.decode(42))
  }

  property("composition") = kleisli && or && opt

  def succeeds[A](a: A, result: Either[Seq[Throwable], A]): Prop = result match {
    case Right(ra) => a == ra
    case Left(_) => falsified
  }

}
