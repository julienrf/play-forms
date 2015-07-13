package julienrf.forms.manual

object Installation extends Document {
  val document = s"""
## Installation

This software is released on the maven central repository, with organization name `org.julienrf`. Currently, three
artifacts are published:

- `play-forms`: the core library,
- `play-forms-twirl`: the [Twirl](https://github.com/playframework/twirl) integration,
- `play-forms-scalatags`: the [ScalaTags](http://lihaoyi.github.io/scalatags/) integration.

If you are using sbt, add the following dependency to your build:

~~~ language-scala
libraryDependencies += "org.julienrf" %% "play-forms-twirl" % "$version"
~~~

The $version version is compatible with Scala 2.11 and Play 2.4.
"""
}
