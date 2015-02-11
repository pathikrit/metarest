package com.github.pathikrit

import scala.annotation.StaticAnnotation

class MetaRest extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro MetaRest.impl
}

object MetaRest {
  sealed trait MethodAnnotations extends StaticAnnotation
  class get extends MethodAnnotations
  class put extends MethodAnnotations
  class post extends MethodAnnotations
  class patch extends MethodAnnotations

  def impl(c: macros.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def withCompileError[A](msg: String)(block: => A): A = try {
      block
    } catch {
      case _: MatchError => c.abort(c.enclosingPosition, s"@MetaRest: $msg")
    }

    def generateModels(originalFields: List[ValDef]) = {
      val modelNames = List("get", "post", "put", "patch")

      val newFields = originalFields.map(_.mods.annotations).zip(originalFields) flatMap { case (annotations, field) =>
       annotations.map(_ -> field) collect {
          case (q"new patch", q"$accessor val $vname: $tpe") => "patch" -> q"$accessor val $vname: Option[$tpe] = None"
          case (q"new $annotation", f) if modelNames contains annotation.toString => annotation.toString -> f.duplicate
        }
      }

      newFields.groupBy(_._1) map { case (annotation, values) =>
        val (className, classFields) = (macros.toTypeName(c)(annotation.capitalize), values.map(_._2))
        q"@com.kifi.macros.json case class $className(..$classFields)" //TODO: Switch back to jsonstrict once this is fixed: https://github.com/kifi/json-annotation/issues/7
      }
    }

    def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None) = {
      val (className, fields) = withCompileError("must annotate a case class") {
        val q"case class $className(..$fields) extends ..$bases { ..$body }" = classDecl
        (className, fields)
      }

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
