# play-forms [![Build Status](https://travis-ci.org/julienrf/play-forms.svg)](https://travis-ci.org/julienrf/play-forms) [![Codacy Badge](https://www.codacy.com/project/badge/e3bfa9fc866b4f5bb418b52da6733297)](https://www.codacy.com/app/julien_2/play-forms) [![Stories in Ready](https://badge.waffle.io/julienrf/play-forms.png?label=ready&title=Ready)](https://waffle.io/julienrf/play-forms) [![Join the chat at https://gitter.im/julienrf/play-forms](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/julienrf/play-forms?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Another way to handle HTML forms in your Play application.

This is a [work in progress](https://waffle.io/julienrf/play-forms).

## Motivation

The design of the Play built-in form API suffers from several flaws:

- `Form`s do not **compose** (ie. you can not define more complex forms from simpler forms) ;
- Form UI and form model **duplicate** a lot of things ;
- Extensibility _via_ `FieldConstructor` is **limited** due to inversion of control ;
- Field selection is string-based (and therefore not **type-safe** nor refactoring-proof) ;
- The HTML form helpers do not correctly handle optional and required fields ([ref](https://groups.google.com/d/topic/play-framework/ziV3_wnAWX0/discussion)) ;
- The whole API is **tightly coupled** with the i18n API.

play-forms aims to solve all these issues.

## Installation and Usage

[play-forms documentation](https://play-forms-doc.herokuapp.com/).

## Changelog

NA

## License

This content is released under the [MIT License](http://opensource.org/licenses/mit-license.php).
