package julienrf.forms.rules

import julienrf.forms.FormData
import org.scalacheck.{Prop, Properties}
import org.scalacheck.Prop._

import scala.util.{Failure, Success, Try}

object RuleTest extends Properties("Rule") {

  val text = forAll { (s: String, k: String) =>
    (k.nonEmpty && s.nonEmpty) ==> {
      val rule = UsualRules.text
      succeeds(s, rule.run(data(k, s))) &&
      rule.run(noData(k)).isFailure &&
      rule.run(emptyData(k)).isFailure
    }
  }
  val int = forAll { (n: Int, k: String) =>
    k.nonEmpty ==> {
      val rule = UsualRules.int
      succeeds(n, rule.run(data(k, rule.show(n)))) &&
      rule.run(noData(k)).isFailure
    }
  }
  val min = forAll { (n: Int, m: Int, k: String) =>
    k.nonEmpty ==> {
      val rule = UsualRules.min(m)
      val result = rule.run(n)
      if (n >= m) succeeds(n, result) else result.isFailure
    }
  }

  property("usual rules") = text && int && min

  val kleisli = undecided
  val or = undecided
  val opt = {
    // FIXME I’d like to write forAll { (rule: Rule[A, B], a: A, b: B) => ... }
    val rule = UsualRules.min(42)
    rule.run(0).isFailure && succeeds(None, rule.?.run(0)) &&
    succeeds(42, rule.run(42)) && succeeds(Some(42), rule.?.run(42))
  }

  property("composition") = kleisli && or && opt

  def data(k: String, v: String): (FormData, String) = (Map(k -> Seq(v)), k)
  def noData(k: String): (FormData, String) = (Map.empty[String, Seq[String]], k)
  def emptyData(k: String): (FormData, String) = (Map(k -> Seq("")), k)
  def succeeds[A](a: A, result: Try[A]): Prop = result match {
    case Success(ra) => a == ra
    case Failure(_) => falsified
  }
  def failed[A](result: Try[A]): Prop = result.isFailure

}
