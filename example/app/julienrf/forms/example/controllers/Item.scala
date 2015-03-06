package julienrf.forms.example.controllers

import julienrf.forms.Form.{field, form}
import julienrf.forms.rules.UsualRules._
import julienrf.forms.presenters.Input.input
import julienrf.forms.presenters.Select.{select, options, enumOptions}
import julienrf.forms.{Form, FormUi}
import play.api.http.Writeable
import play.api.libs.functional.syntax._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scalatags.Text.Tag

case class Item(detail: ItemDetail, category: Category)

case class ItemDetail(name: String, price: Int, description: Option[String])

sealed trait Category
case object Gardening extends Category
case object Furniture extends Category

object Category {

  val values: Set[Category] = Set(Gardening, Furniture) // TODO Write a macro for that

  // TODO Write a macro for that
  val keys: Category => String = {
    case Gardening => "gardening"
    case Furniture => "furniture"
  }

  // TODO Write a macro for that
  val labels: Category => String = {
    case Gardening => "Gardening"
    case Furniture => "Furniture"
  }

  val valuesToKey: Map[Category, String] = (values map (v => v -> keys(v))).toMap
}

object Item extends Controller {

  /**
   * A form model definition for Item.
   *
   * The definition aggregates several fields. Each field is represented by:
   *   - a key,
   *   - a validation rule,
   *   - an HTML user interface.
   *
   * In the following code the user interface is just an HTML `input` tag.
   */
  val itemDetailForm = (
    field("name", text)(input) ~ // A text field
    field("price", int >=> min(42))(input) ~ // A number that must be greater or equal to 42
    field("description", text.?)(input) // An optional text field
  )(ItemDetail.apply, unlift(ItemDetail.unapply))

  // itemForm has type Form[Item], that is a form that handles Items
  itemDetailForm: Form[ItemDetail]

  /**
   * The form definition for the whole item assembles the `ìtemDetailForm` form with a category field.
   */
  val itemForm = (
    form("detail", itemDetailForm) ~
    field("category", oneOf(Category.valuesToKey))(select(options(enumOptions(Category.values, Category.keys, Category.labels))))
  )(Item.apply, unlift(Item.unapply))

  /**
   * Generate the HTML markup of the form:
   *
   * {{{
   *   <form action="/" method="POST">
   *     <input type="text" name="name" required="required" />
   *     <input type="number" name="price" min="42" required="required" />
   *     <input type="text" name="description" />
   *     <select required="required">
   *       <option value=""></option>
   *       <option value="gardening">Gardening</option>
   *       <option value="furniture">Furniture</option>
   *     </select>
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
    val item = Item(ItemDetail("foo", 50, Some("description")), Furniture)
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

  val submission2 = Action(bodyParser(itemDetailForm, htmlForm)) { request =>
    val item = request.body
    Ok(item.toString)
  }

  def bodyParser[A](form: Form[A], html: FormUi => Tag): BodyParser[A] = form.bodyParser(errors => Future.successful(BadRequest(html(errors))))

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
