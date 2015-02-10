package com.github.pathikrit

/**
 * In Scala 2.10, use legacy Context (doesn't know about blackbox/whitebox contexts introduced in Scala 2.11)
 */
package object macros {
  type Context = scala.reflect.macros.Context
}
