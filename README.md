# play-forms

Another way to handle HTML forms in your Play application. See the [features](https://github.com/julienrf/play-forms/tree/master/forms/src/test/scala/julienrf/forms)
for more details, or just dig in the code of the [example](https://github.com/julienrf/play-forms/tree/master/example).

This is a work in progress. Next steps are the following:

- [x] different kinds of HTML controls (input, select, etc.)
- [x] tie `Input.Field` and `Input` together
- [x] form composition
- [ ] richer UI controls (with validation error messages, labels, etc.)
- [ ] field value should be `Option[String]` rather than `String`
- [ ] `A` => `Rule[(A, B), C]` => `Rule[B, C]`

## Motivation

The design of the Play built-in form API suffers from several flaws:

- `Form`s do not compose ;
- Form UI and form model duplicate a lot of things ;
- Extensibility _via_ `FieldConstructor` is limited due to inversion of control ;
- Field selection is string-based (and therefore not refactoring-proof) ;
- The whole API is tightly coupled with the i18n API.

## Installation

This software artifact is released available on the maven central repository, with organization name `org.julienrf` and artifact name `play-forms`.

If you are using sbt add the following dependency:

```scala
libraryDependencies += "org.julienrf" %% "play-forms" % "0.0.0-SNAPSHOT"
```

The 0.0.0-SNAPSHOT version is compatible with Scala 2.11 and Play 2.3.x.

## Quick Start

### Forms

#### Definition

The main abstraction is given by the `[Form[A]](http://julienrf.github.io/play-forms/0.0.0-SNAPSHOT/api/#julienrf.forms.Form)`
type. A `Form[A]` is both a way to process a form submission to yield an `A` value **and** a way to display the form user interface.

The simplest way to build such a value is to build a form with just one field:

```scala
import julienrf.forms.Form
import julienrf.forms.rules.Rule
import julienrf.forms.presenters.Input

val nameForm = Form.field("name", Rule.text)(Input.input)
```

`nameForm` is a form with one field, which is itself defined by three values:

- a **key**, `"name"` ;
- a validation **rule**, `Rule.text`, that defines how to get an effective value from data of the form submission. Here,
`Rule.text` defines a computation that tries to get a `String` value ;
- a **presenter**, `Input.input`, that defines how to display the form user interface. Here, `Input.input` simply uses
an `<input>` HTML tag.

#### Display

To display an empty form (that is, a form that is not filled), use the `empty` method (here using [scalatags](https://github.com/lihaoyi/scalatags)):

```scala
import scalatags.Text.all._

form(action := "/submit", method := "POST")(
  nameForm.empty.html,
  button("Submit")
)
```

Note that the `empty` method just displays the **fields** of the form, not the surrounding `<form>` HTML tag.

The above code produces the following HTML markup:

```html
<form action="/submit", method="POST">
  <input type="text" name="name" required>
  <button>Submit</button>
</form>
```

#### Submission process

The `bind` method takes the submission data and returns either an `A` value, in case of success,
or a presentation of the form with the validation errors. You can use it as follows:

```scala
val submission = Action(parse.urlFormEncoded) { request =>
  userForm.bind(request.body) match {
    case Left(errors) => BadRequest(htmlForm(errors))
    case Right(user) => Ok(user.toString)
  }
}
```

#### Composition

There are two ways to build more complex forms from simple forms like `nameForm`: **aggregating** and **nesting**.

Aggregation is achieved as follows:

```scala
import play.api.libs.functional.syntax._

case class User(name: String, age: Int)

val userForm: Form[User] = (nameForm ~ ageForm)(User.apply, unlift(User.unapply))
```

Here, we combine `nameForm` with an hypothetical `ageForm` value (of type `Form[Int]`) to build a `Form[User]` value.

Forms can also be nested by using the `Form.form` method:

```scala
case class Address(...)
case class User(name: String, address: Address)

val userForm = (
  nameForm ~
  Form.form("address", addressForm)
)(User.apply, unlift(User.unapply))
```

Here, `userForm` nests an hypothetical `addressForm` (of type `Form[Address]`).

### Rules

### Presenters

## Changelog

NA

## License

This content is released under the [MIT License](http://opensource.org/licenses/mit-license.php).