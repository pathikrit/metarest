package com.github.pathikrit

/**
 * In Scala 2.11, Context has been deprecated; use blackbox.Context instead
 */
package object macros {
  type Context = scala.reflect.macros.blackbox.Context
}
