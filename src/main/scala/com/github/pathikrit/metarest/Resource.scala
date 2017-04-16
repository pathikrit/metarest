package com.github.pathikrit.metarest

import scala.annotation.StaticAnnotation
import scala.collection.mutable
import scala.meta._

class get extends StaticAnnotation
class put extends StaticAnnotation
class post extends StaticAnnotation
class patch extends StaticAnnotation

class Resource extends StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val (cls: Defn.Class, companion: Defn.Object) = defn match {
      case q"${cls: Defn.Class}; ${companion: Defn.Object}" => (cls, companion)
      case q"${cls: Defn.Class}" => (cls, q"object ${Term.Name(cls.name.value)} {}")
      case _ => abort("@metarest.Resource must annotate a class")
    }

    val paramsWithAnnotation = for {
      Term.Param(mods, name, decltype, default) <- cls.ctor.paramss.flatten
      seenMods = mutable.Set.empty[String]
      modifier <- mods if seenMods.add(modifier.toString)
      (tpe, defArg) <- modifier match {
        case mod"@get" | mod"@put" | mod"@post" => Some(decltype -> default)
        case mod"@patch" =>
          val optDeclType = decltype.collect({case tpe: Type => targ"Option[$tpe]"})
          val defaultArg = default match {
            case Some(term) => q"Some($term)"
            case None => q"None"
          }
          Some(optDeclType -> Some(defaultArg))
        case _ => None
      }
    } yield modifier -> Term.Param(Nil, name, tpe, defArg)

    val models = paramsWithAnnotation
      .groupBy(_._1.toString)
      .map({case (verb, pairs) =>
        val className = Type.Name(verb.stripPrefix("@").capitalize)
        val classParams = pairs.map(_._2)
        q"case class $className[..${cls.tparams}] (..$classParams)"
      })

    val newCompanion = companion.copy(
      templ = companion.templ.copy(stats = Some(
        companion.templ.stats.getOrElse(Nil) ++ models
      ))
    )

    q"$cls; $newCompanion"
  }
}
