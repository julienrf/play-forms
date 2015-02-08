package julienrf.forms

import org.scalacheck.{Arbitrary, Gen}
import play.api.data.mapping.{KeyPathNode, Path}

object Generators {

  implicit val pathGen: Arbitrary[Path] = Arbitrary {
    val nodes = Gen.nonEmptyListOf(Gen.alphaChar).map(chars => KeyPathNode(chars.mkString))
    Gen.nonEmptyListOf(nodes).map(nodes => Path(nodes))
  }

}
