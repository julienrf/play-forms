package julienrf.forms.manual

class Forms {
"""
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

This expression returns the following markup:

~~~ html
${emptyFieldCode.value.html.mkString}
~~~

Note that the input type has automatically been set to `text` and that a validation attribute `required` has
automatically been added, consistently with the codec definition for the field.

Also note that the `empty` method just displays the **fields** of the form, not the surrounding `<form>` HTML tag. Here is
an example of code showing a complete form form:

${source(showNameFormCode)}

### Submission process

The `decode` method takes the submission data and returns either an `A` value, in case of success,
or a presentation of the form with the validation errors. You can use it as follows:

~~~ scala
${actionCode.source}
~~~

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


"""
}
