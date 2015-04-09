package julienrf.forms.manual

import play.api.mvc.Results.Ok
import play.api.mvc.Action
import Controller.writeableTag

object GettingStarted extends Document {

  val userDataCode = CodePresenter(new {
    case class UserData(name: String, age: Int)
  })

  import userDataCode.value.UserData
  //  val userForm = Form(mapping(
  //    "name" -> nonEmptyText,
  //    "age" -> number.verifying(Constraints.min(0, strict = true))
  //  )(UserForm.apply)(UserForm.unapply))
  val userFormCode = CodePresenter (new {
    import julienrf.forms.Form.field
    import julienrf.forms.codecs.Codec.{int, text}
    import julienrf.forms.codecs.Constraint.min
    import julienrf.forms.presenters.PlayField.input
    import play.api.libs.functional.syntax._

    val userForm = (
      field("name", text)(input(label = "Name")) ~
      field("age", int >=> min(0))(input(label = "Age"))
    )(UserData.apply, unlift(UserData.unapply))
  })

  object routes {
    object Application {
      def userPost() = julienrf.forms.manual.routes.GettingStarted.userPost()
    }
  }

//  @(form: Form[UserForm])
//    @helper.form(routes.Application.userPost()) {
//      @helper.inputText(form("name"), '_label -> "Name")
//      @helper.inputText(form("age"), '_label -> "Age")
//      <button>Save</button>
//    }
    val formHtmlCode = CodePresenter (new {
      import julienrf.forms.FormUi
      import julienrf.forms.presenters.ScalaTags.Bundle._
      import julienrf.forms.presenters.ScalaTags.form

      def user(userFormUi: FormUi) =
        form(routes.Application.userPost())(
          userFormUi.html,
          <.button("Save")
        )
    })

//  val showForm = Action {
//    Ok(views.html.user(userForm))
//  }
  import userFormCode.value.userForm
  object views {
    object html {
      val user = formHtmlCode.value.user _
    }
  }
  val showFormCode = CodePresenter {
    val showForm = Action {
      Ok(views.html.user(userForm.empty))
    }
  }

//  val create = Action { implicit request =>
//    CreateItem.form.bindFromRequest().fold(
//      formWithErrors => BadRequest(views.html.user(formWithErrors))
//    , user => Ok(user.toString)
//    )
//  }
  import play.api.mvc.Action
  import play.api.mvc.BodyParsers.parse
  import play.api.mvc.Results.{BadRequest, Ok}
  val userPostCode = CodePresenter (new {
    val userPost = Action(parse.urlFormEncoded) { request =>
      userForm.decode(request.body) match {
        case Right(item) => Ok(item.toString)
        case Left(errors) => BadRequest(views.html.user(errors))
      }
    }
  })

  val userPost = userPostCode.value.userPost

  val document = s"""
## Getting Started

The library is built around three main concepts: **forms**, **codecs** and **presenters**.

Let’s illustrate these concepts with a simple form example borrowed from the
[Play documentation](https://www.playframework.com/documentation/2.3.x/ScalaForms).

### Defining a Form

First, define a case class that contains the data you want in the form:

${Document.source(userDataCode)}

Then, define a `Form[UserData]` value defining the decoding process of the form data and the HTML markup of the form:

${Document.source(userFormCode)}

This code defines an HTML form with two fields. Each field is defined by a **key**, a **codec** and
a **presenter**. The key is a unique identifier for the field, the codec defines how to decode and encode the form data,
and the presenter defines how to display the field to the client.

Let’s have a closer look at the first field, whose key is `name`. Its codec is `text`, which tries to read the
field data as a (non empty) `String` value. Finally, its presenter is `input(label = "Name")`, which produces an HTML
markup similar to the `inputText` Play helper.

The definition of the `age` field is similar, but it is worth noting that its codec also uses a constraint checking that
the decoded number is positive.

### Showing a Form

Forms already handle the presentation logic of their fields. For instance, if you call the `empty` method you get the HTML markup
of the form fields. You can then put this markup within an HTML template showing the surrounding `<form>` tag:

${Document.source(formHtmlCode)}

You can define an action that shows an empty form (i.e. a form that has not been filled yet) as follows:

${Document.source(showFormCode)}

### Decoding and Validating a Form Submission

Finally, you also want to write an action that processes the form submission and shows back the HTML form
with its validation errors in case of failure:

${Document.source(userPostCode)}

This action uses the `urlFormEncoded` BodyParser and passes the request body to the form’s `decode` method.
This one tries to decode and validate each field. If they all succeed, it returns a `Right` value containing
a `UserData`. If one or more fields fails, it returns a `Left` value containing the form HTML markup with
the validation errors.

### Try it!

Here is the output of the above code:

${views.html.user(userForm.empty)}

"""
}
