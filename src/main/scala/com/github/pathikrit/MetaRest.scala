package com.github.pathikrit

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros._

class MetaRest extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro MetaRest.impl
}

object MetaRest extends Enumeration {
  type Verb = Value
  val Get, Post, Patch, Put = Value

  class Method(operations: Verb*) extends StaticAnnotation

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def modifiedCompanion(compDeclOpt: Option[ModuleDef], className: TypeName) = {
      compDeclOpt map { compDecl =>
        // Add the formatter to the existing companion object
        val q"object $obj extends ..$bases { ..$body }" = compDecl
        q"""
          object $obj extends ..$bases {
            ..$body
            case class Get(id: Int)
          }
        """
      } getOrElse {
        // Create a companion object with the formatter
        q"""
            object ${className.toTermName} {
              case class Get(id: Int)
            }
         """
      }
    }

    def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None) = {
      val q"case class $className(..$fields) extends ..$bases { ..$body }" = classDecl

      val compDecl = modifiedCompanion(compDeclOpt, className)

      c.Expr(q"""
        $classDecl
        $compDecl
      """)
    }

    annottees.map(_.tree) match {
      case (classDecl: ClassDef) :: Nil => modifiedDeclaration(classDecl)
      case (classDecl: ClassDef) :: (compDecl: ModuleDef) :: Nil => modifiedDeclaration(classDecl, Some(compDecl))
      case _ => c.abort(c.enclosingPosition, "Invalid annottee")
    }

  }
}
