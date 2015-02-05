package julienrf.forms.ui

import julienrf.forms.Reads
import julienrf.forms.rules.UsualRules.text
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll
import play.api.data.mapping.Path
import julienrf.forms.Generators._

object InputTest extends Properties("Input") {

  property("reuse the field path as name attribute") = forAll { (path: Path) =>
    val input = Input.fromReads(Reads(path, text))
    input.toString.containsSlice(s"""name="${path.path.mkString(".")}"""")
  }

}
