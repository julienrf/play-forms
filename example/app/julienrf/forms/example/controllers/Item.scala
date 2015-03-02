package julienrf.forms.example.controllers

import julienrf.forms.Form.field
import julienrf.forms.rules.UsualRules._
import julienrf.forms.ui.Ui.input
import julienrf.forms.FormUi
import play.api.http.Writeable
import play.api.libs.functional.syntax._
import play.api.mvc.{Action, Codec, Controller}

import scalatags.Text.Tag

case class Item(name: String, price: Int, description: Option[String])

object Item extends Controller {

  /**
   * A form model definition for Item.
   *
   * The definition aggregates several fields. Each field is represented by:
   *   - a key,
   *   - a validation rule,
   *   - an HTML user interface.
   *
   * In the following code the user interface is just the HTML `input` tag.
   */
  val itemForm = (
    field("name", text, input) ~ // A text field
    field("price", int >=> min(42), input) ~ // A number that must be greater or equal to 42
    field("description", text.?, input) // An optional text field
  )(Item.apply, unlift(Item.unapply))

  /**
   * Generate the HTML markup of the form:
   *
   * {{{
   *   <form action="/" method="POST">
   *     <input type="text" name="name" required="required" />
   *     <input type="number" name="price" min="42" required="required" />
   *     <input type="text" name="description" />
   *   </form>
   * }}}
   *
   * Note that the input names just reuse those of the form definition, the HTML validation attributes
   * are derived from the validation rules and the input types are derived from the field types.
   */
  val create = Action {
    Ok(htmlForm(itemForm.empty))
  }

  /**
   * Similarly, generate the HTML markup of a pre-filled form using the `unbind` method.
   */
  val edit = Action {
    val item = Item("foo", 50, Some("description"))
    Ok(htmlForm(itemForm.unbind(item)))
  }

  /**
   * Handle form submission: if the binding process fails the form is displayed with validation errors,
   * otherwise the value bound to the form submission is displayed.
   */
  val submission = Action(parse.urlFormEncoded) { request =>
    itemForm.bind(request.body) match {
      case Left(errors) => BadRequest(htmlForm(errors))
      case Right(item) => Ok(item.toString)
    }
  }

  implicit val writeableTag: Writeable[Tag] = Writeable((tag: Tag) => tag.toString().getBytes(Codec.utf_8.charset), Some(HTML))

  /**
   * HTML template for the form
   * @return A form tag containing the given fields and a submit button
   */
  def htmlForm(fields: FormUi): Tag = {
    import scalatags.Text.{attrs, tags}
    import scalatags.Text.all._
    val call = routes.Item.submission()
    tags.form(attrs.action := call.url, attrs.method := call.method)(
      fields.html,
      tags.button("Submit")
    )
  }

}
