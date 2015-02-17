# play-forms

Another way to handle HTML forms in your Play application. See the [features](https://github.com/julienrf/play-forms/blob/master/forms/src/test/scala/julienrf/forms/ui/InputTest.scala) for more details, or just dig in the code of the [example](https://github.com/julienrf/play-forms/tree/master/example).

## Motivation

The design of the Play built-in form API suffers from several flaws:

- It leads to a lot of **code duplication**:
    - the HTML UI can not be derived from the `Form` definition ;
    - `Form`s do not compose ;
- It is not **type safe** (it is therefore not refactoring-proof).
- `FieldConstructor` is a mess.

## Installation

This software artifact is released available on the maven central repository, with organization name `org.julienrf` and artifact name `play-forms`.

If you are using sbt add the following dependency:

```scala
libraryDependencies += "org.julienrf" %% "play-forms" % "0.0.0-SNAPSHOT"
```

The 0.0.0-SNAPSHOT version is compatible with Scala 2.11 and Play 2.3.x.

## Quick Start

> Note that the following example uses [scala-records](https://github.com/scala-records/scala-records).

### 1. Define your form model

```scala
val formModel = Rec(
  name = (Path \ "name").read(text),
  price = (Path \ "price").read(int >>> min(1)),
  description = (Path \ "description").read(opt(text))
)
```

This code defines a form model containing three fields:

- `name`, a text field ;
- `price`, a number field that must be positive ;
- `description`, an optional text field.

### 2. Derive the HTML `input` tags for these fields

```scala
val formUi = Rec(
  name = Input.fromReads(formModel.name),
  price = Input.fromReads(formModel.price),
  description = Input.fromReads(formModel.description)
)
```

This code defines a record containing the three input tags corresponding to each field of the form model.

The HTML representation of these tags is the following:

```html
<input type="text" name="name" required />
<input type="number" name="price" min="1" required />
<input type="text" name="description" />
```

Note that the HTML validation attributes (`required`, `min`), the `type` attribute and the `name` attribute are automatically set according to the form model.

### 3. Use (or write) combinators to define the complete UI of your form

Here is how you can write a Play action that displays the form:

```scala
val showForm = Action {
  val submissionRoute = routes.MyController.handleSubmission()
  Ok(Ui.form(submissionRoute)(formUi.name, formUi.price, formUi.description))
}
```

### 4. Handle the form submission

```scala
val handleSubmission = Action { request =>
  formModel.bind(request) match {
    case Success(data) => Ok
    case Failure(_) => BadRequest
  }
}
```

## Changelog

NA

## License

This content is released under the [MIT License](http://opensource.org/licenses/mit-license.php).