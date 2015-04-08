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
      <.meta(%.charset := "utf-8"),
      <.meta(%.name := "viewport", %.content := "width=device-width,initial-scale=1"),
      <.link(%.rel := "stylesheet", %.href := controllers.routes.Assets.versioned("prism.css").url),
      <.link(%.rel := "stylesheet", %.href := controllers.routes.Assets.versioned("style.css").url),
      "title".tag.apply("play-forms")
    ),
    <.body(
      <.h1(<.a(%.href := "/")("play-forms")),
      "article".tag.apply(raw(pegdown.markdownToHtml(document.document))),
      <.script(%.src := controllers.routes.Assets.versioned("prism.js").url)
    )
  )

   implicit val writeableTag: Writeable[scalatags.Text.Tag] =
     Writeable((tag: scalatags.Text.Tag) => ("<!DOCTYPE html>" ++ tag.toString()).getBytes(Codec.utf_8.charset), Some(HTML))

  val gettingStartedForm = GettingStarted.userPostCode.value.userPost
 }
