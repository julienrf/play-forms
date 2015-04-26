package julienrf.forms.manual

import org.pegdown.{Extensions, PegDownProcessor}
import play.api.mvc.Action
import play.twirl.api.{Html, HtmlFormat}

object Controller extends play.api.mvc.Controller {

  val pegdown = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS)

  val index = serve(None)

  def page(name: String) = serve(Some(name))

  def serve(maybeName: Option[String]) = Action {
    def render(document: Document): Html =
      HtmlFormat.raw(pegdown.markdownToHtml(document.document))
    maybeName match {
      case Some(name) =>
        Index.documents.get(name) match {
          case Some(document) => Ok(html.layout(render(document)))
          case None => NotFound
        }
      case None => Ok(html.layout(render(Index)))
    }
  }

  val gettingStartedForm = QuickStart.userPostCode.value.userPost
 }
