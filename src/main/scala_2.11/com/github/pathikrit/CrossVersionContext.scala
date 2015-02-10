package com.github.pathikrit

import scala.reflect.macros._
import scala.language.experimental.macros

/**
 * Context has been deprecated in Scala 2.11, blackbox.Context is used instead
 */
object CrossVersionDefs {
  type CrossVersionContext = blackbox.Context
}
