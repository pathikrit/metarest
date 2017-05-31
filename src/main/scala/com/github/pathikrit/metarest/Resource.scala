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
    val (cls, companion) = defn match {
      case q"${cls: Defn.Class}; ${companion: Defn.Object}" => (cls, companion)
      case cls: Defn.Class => (cls, q"object ${Term.Name(cls.name.value)}")
      case _ => abort("@metarest.Resource must annotate a class")
    }

    val paramsWithAnnotation = for {
      param <- cls.ctor.paramss.flatten
      seenMods = mutable.Set.empty[String]
      modifier <- param.mods if seenMods.add(modifier.toString)
      newParam <- modifier match {
        case mod"@get" | mod"@put" | mod"@post" => Some(param.copy(mods = Nil))
        case mod"@patch" => param.decltpe collect { case tpe: Type =>
          val defaultArg = param.default match {
            case Some(term) => q"Some($term)"
            case None => q"None"
          }
          param"${param.name}: Option[$tpe] = $defaultArg"
        }
        case _ => None
      }
    } yield modifier -> newParam

    val models = paramsWithAnnotation
      .groupBy(_._1.toString)
      .map({case (verb, pairs) =>
        val className = Type.Name(verb.stripPrefix("@").capitalize)
        val classParams = pairs.map(_._2)
        q"case class $className[..${cls.tparams}] (..$classParams)"
      })

   val newCompanion = companion.copy(
      templ = companion.templ.copy(
        stats = Some(companion.templ.stats.getOrElse(Nil) ++ models)
      )
    )

    q"$cls; $newCompanion"
  }
}
