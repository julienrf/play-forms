package julienrf.forms.manual

object Installation extends Document {
  val document = s"""
## Installation

This software is released on the maven central repository, with organization name `org.julienrf` and artifact name
`play-forms`.

If you are using sbt, add the following dependency to your build:

~~~ language-scala
libraryDependencies += "org.julienrf" %% "play-forms" % "$version"
~~~

The $version version is compatible with Scala 2.11 and Play 2.4.
"""
}
