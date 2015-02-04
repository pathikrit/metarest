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
      val result = fields.flatMap {field =>
        field.mods.annotations.collect {   // TODO: shorten this - make use of the fact that all extend sealed trait MethodAnnotations
          case q"new get" => "get" -> field
          case q"new put" => "put" -> field
          case q"new post" => "post" -> field
          case q"new patch" => "patch" -> field
        }
      } groupBy (_._1) mapValues(_ map (_._2.duplicate))

      println(result("get"))

      // TODO: ----------------------------------------------------------------------------------------
      val getRequestModel = q"""case class Get(id: Int, name: String, email: String)"""
      val postRequestModel = q"""case class Post(name: String, email: String)"""
      val patchRequestModel = q"""case class Patch(name: Option[String])"""
      // ------------------------------------------------------------------------------------------------

      compDeclOpt map { compDecl =>
        val q"object $obj extends ..$bases { ..$body }" = compDecl
        q"""
          object $obj extends ..$bases {
            ..$body
            $getRequestModel
            $postRequestModel
            $patchRequestModel
          }
        """
      } getOrElse {
        q"""
          object ${className.toTermName} {
            $getRequestModel
            $postRequestModel
            $patchRequestModel
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
