package julienrf.forms

import org.scalacheck.{Arbitrary, Gen}
import play.api.data.mapping.Path

object Generators {

  implicit val pathGen: Arbitrary[Path] = Arbitrary(
    Gen.oneOf(Path \ "foo", Path \ "bar", Path \ "foo" \ "bar")
  )

}
