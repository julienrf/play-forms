package julienrf.forms.manual.controllers

import julienrf.forms.manual.CodePresenter
import play.api.mvc.{Action, Controller}
import julienrf.forms.presenters.ScalaTags.bundle._

object Manual extends Controller {

  val version = "0.0.0-SNAPSHOT"

  val index = Action {

    val nameFormCode = CodePresenter(new {
      import julienrf.forms.Form
      import julienrf.forms.codecs.Codec$
      import julienrf.forms.presenters.Input

      val nameForm = Form.field("name", Codec.text)(Input.input)
    })

    import nameFormCode.value.nameForm
    val showNameFormCode = CodePresenter {
      import scalatags.Text.all._

      form(action := "/submit", method := "POST")(
        nameForm.empty.html,
        button("Submit")
      )
    }

    def title(content: Modifier*): Tag = <.h1(content: _*)
    def link(content: Modifier*)(url: String): Tag = <.a(%.href := url)(content: _*)
    def section(content: Modifier*): Tag = <.h2(content: _*)
    def subsection(content: Modifier*): Tag = <.h3(content: _*)
    def p(content: Modifier*): Tag = <.p(content: _*)
    def article(content: Modifier*): Tag = <.div(content: _*)
    def code(content: String): Tag = <.code(content)
    def codeBlock(content: String): Tag = <.pre(<.code(content))

    val page = article(
      p(
        "The library is built arount three main concepts: ", code("Form"), "s, ", code("Rule"), "s and ", code("Presenter"), "s."
      ),
      section("Forms"),
      subsection("Definition"),
      p(
        "The main abstraction is given by the ",
        link(code("Form[A]"))(s"http://julienrf.github.io/play-forms/$version/api/#julienrf.forms.Form"),
        " type. A ", code("Form[A]"), " is both a way to process a form submission to yield "
      ),
      codeBlock(nameFormCode.source),
      subsection("Display"),
      p("To display an empty form (that is, a form that is not filled), use the ", code("empty"), " method:"),
      codeBlock(showNameFormCode.source),
      showNameFormCode.value
    )
    Ok(page.render).as(HTML)
  }

}
