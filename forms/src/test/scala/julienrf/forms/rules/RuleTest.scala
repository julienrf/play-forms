package julienrf.forms.rules

import julienrf.forms.{FieldData, FormData}
import org.scalacheck.{Prop, Properties}
import org.scalacheck.Prop._

import scala.util.{Failure, Success, Try}

object RuleTest extends Properties("Rule") {

  val text = forAll { (s: String) =>
    s.nonEmpty ==> {
      val rule = Rule.text
      succeeds(s, rule.decode(Seq(s))) &&
      rule.decode(Nil).isFailure &&
      rule.decode(Seq("")).isFailure
    }
  }
  val int = forAll { (n: Int) =>
    val rule = Rule.int
    succeeds(n, rule.decode(rule.encode(n).get)) && // TODO Remove `.get`
    rule.decode(Nil).isFailure
  }
  val min = forAll { (n: Int, m: Int) =>
    val rule = Rule.min(m)
    val result = rule.decode(n)
    if (n >= m) succeeds(n, result) else result.isFailure: Prop
  }

  property("usual rules") = text && int && min

  val kleisli = undecided
  val or = undecided
  val opt = {
    // FIXME I’d like to write forAll { (rule: Rule[A, B], a: A, b: B) => ... }
    val rule = Rule.min(42)
    rule.decode(0).isFailure && succeeds(None, rule.?.decode(0)) &&
    succeeds(42, rule.decode(42)) && succeeds(Some(42), rule.?.decode(42))
  }

  property("composition") = kleisli && or && opt

  def succeeds[A](a: A, result: Try[A]): Prop = result match {
    case Success(ra) => a == ra
    case Failure(_) => falsified
  }
  def failed[A](result: Try[A]): Prop = result.isFailure

}
