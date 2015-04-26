package julienrf.forms.example.controllers

import julienrf.forms.twirl.Input.{options, enumOptions}
import julienrf.forms.twirl.Form
import julienrf.forms.twirl.Form.{field, form}
import julienrf.forms.twirl.PlayField.{input, select, checkbox}
import julienrf.forms.twirl.semiGroup
import julienrf.forms.codecs.Codec._
import julienrf.forms.codecs.Constraint.min
import play.api.libs.functional.syntax._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.twirl.api.Html

import scala.concurrent.Future

case class Item(detail: ItemDetail, categories: Seq[Category], isActive: Boolean)

case class ItemDetail(name: String, price: Int, description: Option[String])

sealed trait Category
case object Gardening extends Category
case object Furniture extends Category

object Category {
  // TODO Write a macro for all that
  val values: Set[Category] = Set(Gardening, Furniture)
  val keys: Category => String = { case Gardening => "gardening" case Furniture => "furniture" }
  val labels: Category => String = { case Gardening => "Gardening" case Furniture => "Furniture" }
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
   * In the following code the user interface is similar to the one produced by Play HTML forms helpers
   */
  val itemDetailForm = (
    field("name", text)(input("Name")) ~ // A text field
    field("price", int >=> min(42))(input("Price")) ~ // A number that must be greater or equal to 42
    field("description", text.?)(input("Description")) // An optional text field
  )(ItemDetail.apply, unlift(ItemDetail.unapply))

  // itemForm has type Form[Item], that is a form that handles Items
  itemDetailForm: Form[ItemDetail]

  /**
   * The form definition for the whole item assembles the `ìtemDetailForm` form with a category field (which is a `select` tag).
   */
  val itemForm = (
    form("detail", itemDetailForm) ~
    field("category", severalOf(Category.valuesToKey))(select("Category", options(enumOptions(Category.values, Category.keys, Category.labels)))) ~
    field("active", boolean)(checkbox("Active?"))
  )(Item.apply, unlift(Item.unapply))

  /**
   * Generate the HTML markup of the form
   */
  val create = Action {
    Ok(html.form(itemForm.empty))
  }

  /**
   * Similarly, generate the HTML markup of a pre-filled form using the `unbind` method.
   */
  val edit = Action {
    val item = Item(ItemDetail("foo", 50, Some("description")), Seq(Furniture), isActive = false)
    Ok(html.form(itemForm.render(item)))
  }

  /**
   * Handle form submission: if the binding process fails the form is displayed with validation errors,
   * otherwise the value bound to the form submission is displayed.
   */
  val submission = Action(parse.urlFormEncoded) { request =>
    itemForm.decode(request.body) match {
      case Left(errors) => BadRequest(html.form(errors))
      case Right(item) => Ok(item.toString)
    }
  }

  val submission2 = Action(bodyParser(itemDetailForm, html.form.f)) { request =>
    val item = request.body
    Ok(item.toString)
  }

  def bodyParser[A](form: Form[A], html: Html => Html): BodyParser[A] = form.bodyParser(errors => Future.successful(BadRequest(html(errors))))

}
