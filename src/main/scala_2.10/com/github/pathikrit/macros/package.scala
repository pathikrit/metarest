package com.github.pathikrit

/**
 * Contains code related to macros that compiles without warning in Scala 2.10.x
 */
package object macros {
  type Context = scala.reflect.macros.Context  //use legacy Context
  def asTypeName(c: Context)(name: String) = c.universe.newTypeName(name)  // newTypeName has been deprecated
}
