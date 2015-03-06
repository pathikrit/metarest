package com.github.pathikrit.metarest

import scala.annotation.StaticAnnotation
import scala.reflect.macros.blackbox

package object annotations {
  class Resource extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro Macro.noJson.impl
  }

  class ResourceWithPlayJson extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro Macro.playJson.impl
  }

  class ResourceWithSprayJson extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro Macro.sprayJson.impl
  }

  class get extends StaticAnnotation
  class put extends StaticAnnotation
  class post extends StaticAnnotation
  class patch extends StaticAnnotation

  object JsonMode extends Enumeration {
    val play, spray = Value
  }

  class Macro(jsonMode: Option[JsonMode.Value]) {
    def impl(c: blackbox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
      import c.universe._

      def withCompileError[A](msg: String)(block: => A): A = try block catch {
        case e: MatchError => c.abort(c.enclosingPosition, s"MetaRest: $msg; Got: $e")
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
        newFields.groupBy(_._1) map { case (annotation, values) =>
          val (className, classFields) = (TypeName(annotation.capitalize), values.map(_._2))
          withCompileError("unhandled json mode") {
            jsonMode match {      // TODO: DRY
              case None => q"case class $className(..$classFields)"
              case Some(JsonMode.spray) => q"@us.bleibinha.spray.json.macros.jsonstrict case class $className(..$classFields)"
              case Some(JsonMode.play) => q"@com.kifi.macros.jsonstrict case class $className(..$classFields)"
            }
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

        c.Expr( q"""
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

  object Macro {
    object noJson extends Macro(None)
    object playJson extends Macro(Some(JsonMode.play))
    object sprayJson extends Macro(Some(JsonMode.spray))
  }
}
