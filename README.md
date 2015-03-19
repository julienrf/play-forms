# play-forms

Another way to handle HTML forms in your Play application. See the [features](https://github.com/julienrf/play-forms/tree/master/forms/src/test/scala/julienrf/forms)
for more details, or just dig in the code of the [example](https://github.com/julienrf/play-forms/tree/master/example).

This is a work in progress. Next steps are the following:

- [x] different kinds of HTML controls (input, select, etc.)
- [x] tie `Input.Field` and `Input` together
- [x] form composition
- [x] richer UI controls (with validation error messages, labels, etc.)
- [ ] submission error should display the submitted data
- [ ] use a Reader to ease `Presenter`s composition
- [ ] `Rule[A, B] =:= A => Either[Seq[Throwable], B]`
- [ ] field value should be `Option[String]` rather than `String`
- [ ] JSON presenter for client-side rendering and error reporting
- [ ] `A` => `Rule[(A, B), C]` => `Rule[B, C]`

## Motivation

The design of the Play built-in form API suffers from several flaws:

- `Form`s do not **compose** ;
- Form UI and form model **duplicate** a lot of things ;
- Extensibility _via_ `FieldConstructor` is **limited** due to inversion of control ;
- Field selection is string-based (and therefore not **type-safe** nor refactoring-proof) ;
- The HTML form helpers do not correctly handle optional and required fields ([ref](https://groups.google.com/d/topic/play-framework/ziV3_wnAWX0/discussion)) ;
- The whole API is **tightly coupled** with the i18n API.

play-forms aims to solve all these issues.

## Installation

This software artifact is released available on the maven central repository, with organization name `org.julienrf` and artifact name `play-forms`.

If you are using sbt add the following dependency:

```scala
libraryDependencies += "org.julienrf" %% "play-forms" % "0.0.0-SNAPSHOT"
```

The 0.0.0-SNAPSHOT version is compatible with Scala 2.11 and Play 2.3.x.

## Quick Start

The library is built around three main concepts: `Form`s, `Rule`s and `Presenter`s.

### Forms

#### Definition

The main abstraction is given by the [`Form[A]`](http://julienrf.github.io/play-forms/0.0.0-SNAPSHOT/api/#julienrf.forms.Form)
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

`Rule`s and `Presenter`s are described in the next sections. The remaining of this sections gives more details about `Form`s.

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
  <input type="text" name="name" required />
  <button>Submit</button>
</form>
```

Note that the input type has automatically been set to `text` and that a validation attribute `required` has
automatically been added, consistently with the rule definition for the field.

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

In case of error the above code automatically displays the HTML form with the values filled by the client
and the validation errors.

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

Composition makes it possible to reuse not only the validation rules but also the HTML presentation of a form.

### Rules

Each field of a form is associated to a [`Rule`](http://julienrf.github.io/play-forms/0.0.0-SNAPSHOT/api/#julienrf.forms.rules.Rule).
This one defines the process that validates and transforms the encoded data of the form submission into a high-level
data type.

Rules are designed to be simple, general and composable. A `Rule[A, B]` is essentially a function `A => Try[B]`.

#### Chaining

You can chain several validation rules by using the `andThen` method:

```scala
Rule.int andThen Rule.min(42)
```

This rule first tries to coerce the form data to an `Int` and then checks that it is at least equal to `42`.

Note that you can also use the symbolic alias `>=>` for `andThen`:

```scala
Rule.int >=> Rule.min(42)
```

#### Optionality

The `opt` method turns a `Rule[A, B]` into a `Rule[A, Option[B]]`, that is a rule that turns failures into successful
empty (`None`) values. There is also a symbolic alias `?`:

```scala
Rule.int.?
```

### Presenters

The last piece of the puzzle are [`Presenter`](http://julienrf.github.io/play-forms/0.0.0-SNAPSHOT/api/#julienrf.forms.presenters.Presenter)s.

As its name suggests, a `Presenter[A]` defines how to present a field of type `A` to the client.

play-forms provides some ready to use `Presenter`s (e.g. `Input` and `Select`). They can be useful to quickly bootstrap
a user interface but it is highly probable that you will write a `Presenter` specific to the design of the UI of your
project.

Thankfully, the code of existing `Presenter` is modular and reusable so you won’t have to reinvent the wheel each time.
Also, note that the type parameter `A` gives the opportunity to perform type-level computations to derive some parts of
the UI.

## Changelog

NA

## License

This content is released under the [MIT License](http://opensource.org/licenses/mit-license.php).