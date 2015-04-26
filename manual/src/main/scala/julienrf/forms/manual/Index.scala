package julienrf.forms.manual

object Index extends Document {

  val documents: Map[String, Document] = Map(
    "installation"    -> Installation,
    "quick-start" -> QuickStart
  )

  def findRev(document: Document): Option[String] =
    documents.find(_._2 == document).map(_._1)

  def url(document: Document): String =
    findRev(document).map(name => s"/$name") getOrElse sys.error("Unable to compute URL")

  val document = s"""
## Documentation

- [Installation](${url(Installation)})
- [Quick Start](${url(QuickStart)})
- [API Documentation](http://julienrf.github.io/play-forms/$version/api/)
"""

}
