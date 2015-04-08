package julienrf.forms.manual

import julienrf.forms.manual.CodePresenter.ExprPresentation

trait Document {
  def document: String // Markdown content
}

object Document {

  def source(exprPresentation: ExprPresentation[_]): String = s"""
~~~ language-scala
${exprPresentation.source}
~~~
"""

}