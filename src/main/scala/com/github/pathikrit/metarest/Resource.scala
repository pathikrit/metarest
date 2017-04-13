package com.github.pathikrit.metarest

import scala.collection.immutable.Seq
import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.meta._

class get extends StaticAnnotation
class put extends StaticAnnotation
class post extends StaticAnnotation
class patch extends StaticAnnotation

@compileTimeOnly("@metarest.Resource not expanded")
class Resource extends StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val (cls: Defn.Class, companion: Defn.Object) = defn match {
      case Term.Block(Seq(cls: Defn.Class, companion: Defn.Object)) => (cls, companion)
      case cls: Defn.Class => (cls, q"object ${Term.Name(cls.name.value)} {}")
      case _ => abort("@metarest.Resource must annotate a class")
    }

    val paramsWithAnnotation = for {
      Term.Param(mods, name, decltype, default) <- cls.ctor.paramss.flatten
      modifier <- mods
      newField <- modifier match {
        case mod"@get" | mod"@put" | mod"@post" => Some(Term.Param(Nil, name, decltype, default))
        case mod"@patch" =>
          val optDeclType = decltype.collect({case tpe: Type => targ"Option[$tpe]"})
          val defaultArg = default match {
            case Some(term) => q"Some($term)"
            case _ => q"None"
          }
          Some(Term.Param(Nil, name, optDeclType, Some(defaultArg)))
        case _ => None
      }
    } yield modifier -> newField

    val models = paramsWithAnnotation
      .groupBy(_._1.toString)
      .map({case (verb, pairs) =>
        val className = Type.Name(verb.stripPrefix("@").capitalize)
        val classParams = pairs.map(_._2).groupBy(_.name.value).mapValues(_.head).values.to[collection.immutable.Seq]
        q"case class $className[..${cls.tparams}] (..$classParams)"
      })

    val newCompanion = companion.copy(
      templ = companion.templ.copy(stats = Some(
        companion.templ.stats.getOrElse(Nil) ++ models
      ))
    )

    Term.Block(Seq(cls, newCompanion))
  }
}
