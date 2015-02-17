package com.github.pathikrit

import scala.annotation.StaticAnnotation

class MetaRest extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro MetaRest.impl
}

object MetaRest {
  class   get extends StaticAnnotation
  class   put extends StaticAnnotation
  class  post extends StaticAnnotation
  class patch extends StaticAnnotation

  def impl(c: macros.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def withCompileError[A](msg: String)(block: => A): A = try block catch {
      case _: MatchError => c.abort(c.enclosingPosition, s"@MetaRest: $msg")
    }

    def getClassAndFields(classDecl: ClassDef) = withCompileError("must annotate a case class") {
      val q"case class $className(..$fields) extends ..$bases { ..$body }" = classDecl
      (className, fields)
    }

    def generateModels(fields: List[ValDef]) = {
      val fieldsWithAnnotations = fields.map(_.mods.annotations).zip(fields)
      val newFields = fieldsWithAnnotations flatMap { case (annotations, field) =>
        annotations.map(_ -> field) collect {
          case (q"new patch", q"$accessor val $vname: $tpe") => "patch" -> q"$accessor val $vname: Option[$tpe] = None"
          case (q"new $annotation", f) if Set("get", "post", "put") contains annotation.toString => annotation.toString -> f.duplicate
        }
      }
    }

    def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None) = {
      val (className, fields) = getClassAndFields(classDecl)

      val compDecl = compDeclOpt map { compDecl =>
        val q"object $obj extends ..$bases { ..$body }" = compDecl
        q"""
          object $obj extends ..$bases {
            ..$body
            ..${generateModels(fields)}
          }
        """
      } getOrElse {
        q"""
          object ${className.toTermName} {
            ..${generateModels(fields)}
          }
         """
      }

      c.Expr(q"""
        $classDecl
        $compDecl
      """)
    }

    withCompileError("must annotate a class") {
      annottees.map(_.tree) match {
        case (classDecl: ClassDef) :: Nil => modifiedDeclaration(classDecl)
        case (classDecl: ClassDef) :: (compDecl: ModuleDef) :: Nil => modifiedDeclaration(classDecl, Some(compDecl))
      }
    }
  }
}
