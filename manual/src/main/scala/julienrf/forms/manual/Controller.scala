package julienrf.forms.manual

import julienrf.forms.presenters.ScalaTags.Bundle._
import org.pegdown.{Extensions, PegDownProcessor}
import play.api.http.Writeable
import play.api.mvc.{Action, Codec}

object Controller extends play.api.mvc.Controller {

  val pegdown = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS)

  val index = serve(None)

  def page(name: String) = serve(Some(name))

  def serve(maybeName: Option[String]) = Action {
    maybeName match {
      case Some(name) =>
        Index.documents.get(name) match {
          case Some(document) => Ok(layout(document))
          case None => NotFound
        }
      case None => Ok(layout(Index))
    }
  }

  def layout(document: Document) = <.html(
    <.head(
      "title".tag.apply("play-forms")
    ),
    <.body(
      <.h1(<.a(%.href := "/")("play-forms")),
      "article".tag.apply(raw(pegdown.markdownToHtml(document.document)))
    )
  )

   implicit val writeableTag: Writeable[scalatags.Text.Tag] =
     Writeable((tag: scalatags.Text.Tag) => ("<!DOCTYPE html>" ++ tag.toString()).getBytes(Codec.utf_8.charset), Some(HTML))

 }
