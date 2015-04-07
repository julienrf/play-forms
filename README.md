# play-forms [![Build Status](https://travis-ci.org/julienrf/play-forms.svg)](https://travis-ci.org/julienrf/play-forms) [![Coverage Status](https://coveralls.io/repos/julienrf/play-forms/badge.svg)](https://coveralls.io/r/julienrf/play-forms) [![Stories in Ready](https://badge.waffle.io/julienrf/play-forms.png?label=ready&title=Ready)](https://waffle.io/julienrf/play-forms) [![Join the chat at https://gitter.im/julienrf/play-forms](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/julienrf/play-forms?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Another way to handle HTML forms in your Play application.

This is a [work in progress](https://waffle.io/julienrf/play-forms).

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

The 0.0.0-SNAPSHOT version is compatible with Scala 2.11 and Play 2.4.x.

## Documentation

[play-forms documentation](https://play-forms-doc.herokuapp.com/).

## Changelog

NA

## License

This content is released under the [MIT License](http://opensource.org/licenses/mit-license.php).