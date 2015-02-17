package julienrf.forms

import play.api.data.mapping.Validation

package object rules {
  type Result[+A] = Validation[Throwable, A]
  type InputData = (Map[String, Seq[String]], String)
}
