package julienrf.forms.manual

class Codecs {
"""
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

"""
}
