package julienrf

package object forms {
  // Currently a type alias
  // In the future it may be more abstract to support JSON or other document types as input
  type FieldData = Seq[String]

  // Idem
  type FormData = Map[String, Seq[String]]
}
