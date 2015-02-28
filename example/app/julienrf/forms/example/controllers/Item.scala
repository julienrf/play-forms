package julienrf.forms.example.controllers

import julienrf.forms.rules.UsualRules._
import julienrf.forms.ui.Ui.input
import julienrf.forms.{Form, Fields}
import play.api.http.Writeable
import play.api.libs.functional.syntax._
import play.api.mvc.{Action, AnyContentAsFormUrlEncoded, Codec, Controller}

import scalatags.Text.Tag

case class Item(name: String, price: Int, description: Option[String])

object Item extends Controller {

  val itemForm = (
    Form("name", text, input) ~
    Form("price", int >=> min(42), input) ~
    Form("description", text.?, input)
  )(Item.apply, unlift(Item.unapply))

  def show(fields: Fields): Tag = {
    import scalatags.Text.{attrs, tags}
    import scalatags.Text.all._
    val call = routes.Item.submitForm()
    tags.form(attrs.action := call.url, attrs.method := call.method)(
      fields.html,
      tags.button("Submit")
    )
  }

  val showForm = Action {
    // Generates the following markup
    // <form action="/" method="POST">
    //   <input type="text" name="name" required="required" />
    //   <input type="number" name="price" min="42" required="required" />
    //   <input type="text" name="description" />
    // </form>
    Ok(show(itemForm.empty))
  }

  val submitForm = Action { request =>
    request.body match {
      case AnyContentAsFormUrlEncoded(data) =>
        itemForm.bind(data) match {
          case Left(errors) => BadRequest(show(errors))
          case Right(item) => Ok(item.toString)
        }
      case _ => BadRequest
    }
  }

  implicit val writeableTag: Writeable[Tag] = Writeable((tag: Tag) => tag.toString().getBytes(Codec.utf_8.charset), Some(HTML))

}
