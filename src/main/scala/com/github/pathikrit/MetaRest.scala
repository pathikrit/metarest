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

  private[this] implicit class Pairs[A, B](p: List[(A, B)]) {
    def toMultiMap: Map[A, List[B]] = p.groupBy(_._1).mapValues(_.map(_._2))
  }

  def impl(c: macros.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def toTypeName(name: String) = macros.asTypeName(c)(name)

    def generateModels(fields: List[ValDef]): Iterable[Tree] = {
      val annotatedFields = fields flatMap {field =>
        field.mods.annotations collect {
          case q"new $annotation" => annotation.toString -> field.duplicate
        }
      }

      val fieldLookup = annotatedFields.toMultiMap withDefaultValue Nil

      val (gets, posts, puts) = (fieldLookup("get"), fieldLookup("post"), fieldLookup("put"))

      val patches = fieldLookup("patch") collect {
        case q"$accessor val $vname: $tpe" => q"$accessor val $vname: Option[$tpe] = None"
      }

      Map("Get" -> gets, "Post" -> posts, "Put" -> puts, "Patch" -> patches) collect {
        case (name, reqFields) if reqFields.nonEmpty => q"@com.kifi.macros.json case class ${toTypeName(name)}(..$reqFields)"
      } //TODO: Switch back to jsonstrict once this is fixed: https://github.com/kifi/json-annotation
    }

    def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None) = {
      val q"case class $className(..$fields) extends ..$bases { ..$body }" = classDecl

      val requestModels = generateModels(fields)

      val compDecl = compDeclOpt map { compDecl =>
        val q"object $obj extends ..$bases { ..$body }" = compDecl
        q"""
          object $obj extends ..$bases {
            ..$body
            ..$requestModels
          }
        """
      } getOrElse {
        q"""
          object ${className.toTermName} {
            ..$requestModels
          }
         """
      }

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
