package julienrf

/**
 * The library is built around three abstractions: [[julienrf.forms.Form Form]], [[julienrf.forms.codecs.Codec Codec]] and
 * [[julienrf.forms.presenters.Presenter Presenter]].
 *
 * A [[julienrf.forms.Form Form]] combines several [[julienrf.forms.codecs.Codec Codecs]] and [[julienrf.forms.presenters.Presenter Presenters]] to define:
 *  - how to decode the form data ;
 *  - how to display an empty (not filled) form ;
 *  - how to display a filled form with and without validation errors.
 */
package object forms {
  // Currently a type alias
  // In the future it may be more abstract to support JSON or other document types as input
  type FieldData = Seq[String]

  // Idem
  type FormData = Map[String, Seq[String]]
}
