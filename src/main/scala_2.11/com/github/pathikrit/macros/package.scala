package com.github.pathikrit

/**
 * Contains code related to macros that compiles without warnings in Scala 2.11.x
 */
package object macros {
  type Context = scala.reflect.macros.blackbox.Context  // Context has been deprecated; use blackbox.Context instead
  def toTypeName(c: Context)(name: String) = c.universe.TypeName(name)  // newTypeName has been deprecated, uses TypeName.apply
}
