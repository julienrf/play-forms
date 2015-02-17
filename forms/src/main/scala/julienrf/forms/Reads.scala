package julienrf.forms

import julienrf.forms.rules.{Result, InputData, Rule}
import play.api.data.mapping.Path

case class Reads[A](path: Path, rule: Rule[InputData, A]) {
  def bind(data: Map[String, Seq[String]]): Result[A] = rule.run((data, path.path.mkString(".")))
}

object Reads {

  implicit class PathOps(path: Path) {
    def read[A](ra: Rule[InputData, A]): Reads[A] = Reads(path, ra)
  }

}