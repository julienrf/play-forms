package julienrf.forms.rules

import org.scalacheck.Properties
import org.scalacheck.Prop._

object RuleTest extends Properties("Rule") {

  val text = undecided
  val int = undecided
  val min = undecided

  property("usual rules") = min

  val kleisli = undecided
  val or = undecided
  val opt = undecided

  property("composition") = kleisli && or && opt

}
