package julienrf.forms.st

import org.apache.commons.lang3.StringEscapeUtils

object ScalaTags {

  def hasAttr(name: String, maybeValue: Option[String])(html: Seq[scalatags.Text.Modifier]): Boolean = {
    val elem = scalatags.Text.tags.div(html: _*)
    maybeValue match {
      case Some(value) =>
        elem.toString().containsSlice(s"""$name="${StringEscapeUtils.escapeXml11(value)}"""")
      case None =>
        elem.toString().containsSlice(name)
    }
  }

  def render(html: Seq[scalatags.Text.Modifier]): String = {
    import julienrf.forms.presenters.ScalaTags.Bundle._
    <.div(html).render.drop(5).dropRight(6)
  }

}
