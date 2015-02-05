package julienrf.forms.example.controllers

import julienrf.forms.Reads.PathOps
import julienrf.forms.rules.UsualRules._
import julienrf.forms.ui.Ui
import play.api.data.mapping.Path
import play.api.http.Writeable
import play.api.mvc.{Codec, Action, Controller}
import records.Rec

case class Item(name: String, price: Int, description: Option[String])

object Item extends Controller {

  val itemReads = Rec(
    name = (Path \ "name").read(text),
    price = (Path \ "price").read(int >>> min(42)),
    description = (Path \ "description").read(opt(text))
  )

  val itemUi = Rec(
    name = Ui.fromReads(itemReads.name),
    price = Ui.fromReads(itemReads.price),
    description = Ui.fromReads(itemReads.description)
  )

  val form = Action {
    val route = routes.Item.submit()
    // Generates the following markup
    // <form action="/" method="POST">
    //   <input type="text" name="name" required="required" />
    //   <input type="number" name="price" min="42" required="required" />
    //   <input type="text" name="description" />
    // </form>
    Ok(Ui.form(routes.Item.submit())(itemUi.name, itemUi.price, itemUi.description))
  }

  val submit = Action {
    Ok
  }

  implicit val writeableTag: Writeable[scalatags.Text.Tag] = Writeable((tag: scalatags.Text.Tag) => tag.toString().getBytes(Codec.utf_8.charset), Some(HTML))

}
