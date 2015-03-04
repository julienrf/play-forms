# play-forms

Another way to handle HTML forms in your Play application. See the [features](https://github.com/julienrf/play-forms/tree/master/forms/src/test/scala/julienrf/forms) for more details, or just dig in the code of the [example](https://github.com/julienrf/play-forms/tree/master/example).

This is a work in progress. Next steps are the following:

- [x] different kinds of HTML controls (input, select, etc.) ;
- [ ] tie `Input.Field` and `Input` together ;
- [ ] form composition ;
- [ ] richer UI controls (with validation error messages, labels, etc.).

## Motivation

The design of the Play built-in form API suffers from several flaws:

- It leads to a lot of **code duplication**:
    - the HTML UI can not be derived from the `Form` definition ;
    - `Form`s do not compose ;
- It is not **type safe** (it is therefore not refactoring-proof) ;
- `FieldConstructor` is a mess ;
- It is tightly coupled with the i18n API.

## Installation

This software artifact is released available on the maven central repository, with organization name `org.julienrf` and artifact name `play-forms`.

If you are using sbt add the following dependency:

```scala
libraryDependencies += "org.julienrf" %% "play-forms" % "0.0.0-SNAPSHOT"
```

The 0.0.0-SNAPSHOT version is compatible with Scala 2.11 and Play 2.3.x.

## Quick Start

TBD

## Changelog

NA

## License

This content is released under the [MIT License](http://opensource.org/licenses/mit-license.php).