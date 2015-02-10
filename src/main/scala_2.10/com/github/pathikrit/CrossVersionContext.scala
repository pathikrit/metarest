package com.github.pathikrit

import scala.reflect.macros._
import scala.language.experimental.macros

/**
 * Scala 2.10 uses Context (doesn't know about blackbox and whitebox)
 */
object CrossVersionDefs {
  type CrossVersionContext = Context
}
