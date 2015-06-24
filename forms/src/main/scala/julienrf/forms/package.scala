package julienrf

import julienrf.forms.Presenter

/**
 * The library is built around three abstractions: [[julienrf.forms.Forms.Form Form]], [[julienrf.forms.codecs.Codec Codec]] and
 * [[Presenter Presenter]].
 *
 * A [[julienrf.forms.Forms.Form Form]] combines several [[julienrf.forms.codecs.Codec Codecs]] and [[Presenter Presenters]] to define:
 *  - how to decode the form data ;
 *  - how to display an empty (not filled) form ;
 *  - how to display a filled form with and without validation errors.
 */
package object forms {
  // Currently a type alias
  // In the future it may be more abstract to support JSON or other document types as input
  type FieldData = Option[Seq[String]]

  // Idem
  type FormData = Map[String, Seq[String]]
}
