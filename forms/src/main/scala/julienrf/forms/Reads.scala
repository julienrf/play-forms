package julienrf.forms

import julienrf.forms.rules.Rule
import play.api.data.mapping.Path

case class Reads[A](path: Path, rule: Rule[String, A])

object Reads {

  implicit class PathOps(path: Path) {
    def read[A](ra: Rule[String, A]): Reads[A] = Reads(path, ra)
  }

}