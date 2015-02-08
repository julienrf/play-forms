# play-forms

Another way to handle HTML forms in your Play application. See the [features](https://github.com/julienrf/play-forms/blob/master/forms/src/test/scala/julienrf/forms/InputTest.scala) for more details.

## Motivation

The built-in form API suffers from several flaws leading to a lot of code **duplication**:

- the HTML UI can not be derived from the `Form` definition ;
- `Form`s do not compose.

## Installation

This software artifact is released available on the maven central repository, with organization name `org.julienrf` and artifact name `play-forms`.

If you are using sbt add the following dependency:

```scala
libraryDependencies += "org.julienrf" %% "play-forms" % "0.0.0-SNAPSHOT"
```

The 0.0.0-SNAPSHOT version is compatible with Scala 2.11 and Play 2.3.x.

## Changelog

NA

## License

This content is released under the [MIT License](http://opensource.org/licenses/mit-license.php).