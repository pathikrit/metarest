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
      case cls @ Defn.Class(_, name, _, _, _) => (cls, q"object ${Term.Name(name.value)} {}")
      case _ => abort("@metarest.Resource must annotate a class")
    }

    val Ctor.Primary(_, _, paramss) = cls.ctor

    val fieldsWithModifier = for {
      Term.Param(mods, name, decltype, default) <- paramss.flatten
      modifier <- mods
    } yield modifier match {
      case mod"@get" | mod"@put" | mod"@post" => Some(modifier -> Term.Param(Nil, name, decltype, default))
      case mod"@patch" => Some(modifier -> Term.Param(Nil, name, decltype, None))
      case _ => None
    }

    val fields = fieldsWithModifier.flatten
      .groupBy({case (mod, _) => mod.toString.stripPrefix("@").capitalize})
      .mapValues(values => values.map(_._2))

    val newFields = fields map { case (className, classParams) =>
      q"case class ${Type.Name(className)}(..$classParams)"
    }

    val newCompanion = companion.copy(
      templ = companion.templ.copy(stats = Some(
        companion.templ.stats.getOrElse(Nil) ++ newFields
      ))
    )

    Term.Block(Seq(cls, newCompanion))
  }
}
