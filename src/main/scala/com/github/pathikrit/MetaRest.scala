package com.github.pathikrit

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros._

class MetaRest extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro MetaRest.impl
}

object MetaRest {
  sealed trait MethodAnnotations extends StaticAnnotation
  class get extends MethodAnnotations
  class put extends MethodAnnotations
  class post extends MethodAnnotations
  class patch extends MethodAnnotations

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def modifiedCompanion(compDeclOpt: Option[ModuleDef], className: TypeName, fields: List[ValDef]) = {
      val result = fields flatMap {field =>
        field.mods.annotations.collect {   // TODO: shorten this - make use of the fact that all extend sealed trait MethodAnnotations
          case q"new get" => "get" -> field.name
          case q"new put" => "put" -> field.name
          case q"new post" => "post" -> field.name
          case q"new patch" => "patch" -> field.name
        }
      }

      val restModels = q"""
        case class Get(id: Int)
      """

      compDeclOpt map { compDecl =>
        val q"object $obj extends ..$bases { ..$body }" = compDecl
        q"""
          object $obj extends ..$bases {
            ..$body
            $restModels
          }
        """
      } getOrElse {
        q"""
          object ${className.toTermName} {
            $restModels
          }
         """
      }
    }

    def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None) = {
      val q"case class $className(..$fields) extends ..$bases { ..$body }" = classDecl

      val compDecl = modifiedCompanion(compDeclOpt, className, fields)

      c.Expr(q"""
        $classDecl
        $compDecl
      """)
    }

    annottees.map(_.tree) match {
      case (classDecl: ClassDef) :: Nil => modifiedDeclaration(classDecl)
      case (classDecl: ClassDef) :: (compDecl: ModuleDef) :: Nil => modifiedDeclaration(classDecl, Some(compDecl))
      case _ => c.abort(c.enclosingPosition, "@MetaRest must annotate a class")
    }
  }
}
