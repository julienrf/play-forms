package julienrf.forms.manual

object GettingStarted extends Document {
  import Document._

  val nameFormCode = CodePresenter(new {
    import julienrf.forms.Form
    import julienrf.forms.codecs.Codec
    import julienrf.forms.presenters.Input

    val nameForm = Form.field("name", Codec.text)(Input.input)
  })

  import nameFormCode.value.nameForm
  val emptyFieldCode = CodePresenter {
    nameForm.empty
  }

  val showNameFormCode = CodePresenter {
    import julienrf.forms.presenters.ScalaTags.Bundle._

    <.form(%.action := "/submit", %.method := "POST")(
      nameForm.empty.html,
      <.button("Submit")
    )
  }

  val document = s"""
# Getting Started

The library is built around three main concepts: **forms**, **codecs** and **presenters**.

## Forms

### Definition

The main abstraction is given by the [`Form[A]`](http://julienrf.github.io/play-forms/$version/api/#julienrf.forms.Form)
type. A `Form[A]` is **both** a way to **process** a form submission yielding an `A` value and a way to **render**
the form to the client.

The simplest way to build such a value is to build a form with just one field:

${source(nameFormCode)}

`nameForm` is a form with one field, which is itself defined by three values:

- a **key**, `"name"` ;
- a **codec**, `Codec.text`, that defines how to get an effective value from data of the form submission. Here,
`Codec.text` defines a computation that tries to get a `String` value ;
- a **presenter**, `Input.input`, that defines how to render the form to the client. Here, `Input.input` simply produces
an `<input>` HTML tag.

Codecs and presenters are described in the next sections. The remaining of this section gives more details about
forms.

### Display

To display an empty form (that is, a form that is not filled), use the `empty` method:

${source(emptyFieldCode)}

~~~ html
${emptyFieldCode.value.html.mkString}
~~~

${emptyFieldCode.value.html.mkString}

Note that the `empty` method just displays the **fields** of the form, not the surrounding `<form>` HTML tag. Here is
the complete code showing the empty form:

${source(showNameFormCode)}

The above code produces the following HTML markup:

~~~ html
${showNameFormCode.value.render}
~~~

Note that the input type has automatically been set to `text` and that a validation attribute `required` has
automatically been added, consistently with the codec definition for the field.

### Submission process

The `decode` method takes the submission data and returns either an `A` value, in case of success,
or a presentation of the form with the validation errors. You can use it as follows:

```scala
val submission = Action(parse.urlFormEncoded) { request =>
  userForm.decode(request.body) match {
    case Left(errors) => BadRequest(htmlForm(errors))
    case Right(user) => Ok(user.toString)
  }
}
```

In case of error the above code automatically displays the HTML form with the values filled by the client
and the validation errors.

### Composition

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

## Codecs

Each field of a form is associated to a [`Codec`](http://julienrf.github.io/play-forms/0.0.0-SNAPSHOT/api/#julienrf.forms.codecs.Codec).
This one defines the process that validates and transforms the encoded data of the form submission into a high-level
data type.

Codecs are designed to be simple, general and composable. A `Codec[A, B]` is essentially a function `A => Either[Seq[Throwable], B]`.

### Chaining

You can chain several codecs by using the `andThen` method:

```scala
Codec.int andThen Codec.min(42)
```

This codec first tries to coerce the form data to an `Int` and then checks that it is at least equal to `42`.

Note that you can also use the symbolic alias `>=>` for `andThen`:

```scala
Codec.int >=> Codec.min(42)
```

### Optionality

The `opt` method turns a `Codec[A, B]` into a `Codec[A, Option[B]]`, that is a codec that turns failures into successful
empty (`None`) values. There is also a symbolic alias `?`:

```scala
Codec.int.?
```

## Presenters

The last piece of the puzzle are [`Presenter`](http://julienrf.github.io/play-forms/0.0.0-SNAPSHOT/api/#julienrf.forms.presenters.Presenter)s.

As its name suggests, a `Presenter[A]` defines how to present a field of type `A` to the client.

play-forms provides some ready to use `Presenter`s (e.g. `Input` and `Select`). They can be useful to quickly bootstrap
a user interface but it is highly probable that you will write a `Presenter` specific to the design of the UI of your
project.

Thankfully, the code of existing `Presenter` is modular and reusable so you won’t have to reinvent the wheel each time.
Also, note that the type parameter `A` gives the opportunity to perform type-level computations to derive some parts of
the UI.
"""
}
