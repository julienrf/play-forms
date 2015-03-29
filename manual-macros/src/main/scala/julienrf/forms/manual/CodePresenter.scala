package julienrf.forms.manual

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object CodePresenter {

  // You should not create such a value by yourself. Let the macro do the work
  case class ExprPresentation[A](value: A, source: String)

  def apply[A](a: A): ExprPresentation[A] = macro codeMacro[A]

  def codeMacro[A](c: Context)(a: c.Expr[A]) = {
    import c.universe._

    val ExprPresentation = symbolOf[ExprPresentation[A]].asClass.asType

    val pos = a.tree.pos
    val startLineOffset = pos.source.offsetToLine(pos.start)
    val endLineOffset = pos.source.offsetToLine(pos.end)
    val (startLine, endLine) =
      if (startLineOffset == endLineOffset) (startLineOffset, endLineOffset + 1) // Expression
      else (startLineOffset + 1, endLineOffset) // Block (skip the opening brace)

    if (endLine < startLine) c.abort(c.enclosingPosition, "Please write a code block")

    val codeSource = new String(pos.source.content.slice(pos.source.lineToOffset(startLine), pos.source.lineToOffset(endLine)))
    val indentValue = codeSource.takeWhile(_ == ' ').length

    val deindentedSource =
      codeSource.split('\n')
        .map(line => line.drop(indentValue))
        .mkString("\n")

    a.tree match {
      case q"{ ..$stats }" => q"new $ExprPresentation($a, $deindentedSource)"
      case _ => c.abort(c.enclosingPosition, "Please write a code block")
    }
  }

}
