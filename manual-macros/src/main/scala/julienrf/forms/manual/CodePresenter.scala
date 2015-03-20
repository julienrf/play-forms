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
    val startLine = pos.source.offsetToLine(pos.start) + 1 // Skip the opening brace
    val endLine = pos.source.offsetToLine(pos.end)

    if (endLine < startLine) c.abort(c.enclosingPosition, "Please write a code block")

    val codeSource = new String(pos.source.content.slice(pos.source.lineToOffset(startLine), pos.source.lineToOffset(endLine)))

    a.tree match {
      case q"{ ..$stats }" => q"new $ExprPresentation($a, $codeSource)"
      case _ => c.abort(c.enclosingPosition, "Please write a code block")
    }
  }

}
