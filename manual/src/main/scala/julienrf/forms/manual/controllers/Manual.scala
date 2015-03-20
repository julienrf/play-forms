package julienrf.forms.manual.controllers

import play.api.mvc.{Action, Controller}
import julienrf.forms.presenters.ScalaTags.bundle._

object Manual extends Controller {

  val version = "0.0.0-SNAPSHOT"

  val index = Action {

    object nameFormCode {
      import julienrf.forms.Form
      import julienrf.forms.rules.Rule
      import julienrf.forms.presenters.Input

      val nameForm = Form.field("name", Rule.text)(Input.input)
    }

    import nameFormCode._
    val showNameFormCode = {
      import scalatags.Text.all._

      form(action := "/submit", method := "POST")(
        nameForm.empty.html,
        button("Submit")
      )
    }

    def link(content: Modifier*)(url: String): Modifier = <.a(%.href := url)(content: _*)
    def section(content: Modifier*): Modifier = <.h1(content: _*)
    def subsection(content: Modifier*): Modifier = <.h2(content: _*)
    def p(content: Modifier*): Modifier = <.p(content: _*)
    def article(content: Modifier*): Modifier = <.div(content: _*)
    def code(content: String): Modifier = <.code(content)
    def codeBlock(content: String): Modifier = <.pre(<.code(content))

    val page = article(
      section("Forms"),
      subsection("Definition"),
      p(
        "The main abstraction is given by the ",
        link(code("Form[A]"))(s"http://julienrf.github.io/play-forms/$version/api/#julienrf.forms.Form"),
        " type."
      ),
      codeBlock(
        """TODO get the body of the nameFormCode object definition as a String"""
      ),
      subsection("Display"),
      p("To display an empty form (that is, a form that is not filled), use the ", code("empty"), " method:"),
      codeBlock(
        """TODO get the body of the showNameFormCode val definition as a String"""
      ),
      showNameFormCode
    )
    Ok
  }

}
