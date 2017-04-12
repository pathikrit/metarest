package com.github.pathikrit.metarest

import org.scalatest._, Matchers._
import com.github.pathikrit.metarest._

class MetaRestSuite extends FunSuite {
  test("Generation of Get, Post, Patch, Put models") {
    @Resource case class User(
      @get                id            : Int,
      @get @post @patch   name          : String,
      @get @post          email         : String,
                          registeredOn  : Long
    )

    """User.Get(id = 0, name = "Rick", email = "awesome@msn.com")""" should compile
    """User.Post(name = "Rick", email = "awesome@msn.com")""" should compile
    """User.Put()""" shouldNot compile
    """User.Patch(name = Some("Pathikrit"))""" should compile
    """User.Patch()""" should compile
  }

  test("Complex models") {
    class GET extends scala.annotation.StaticAnnotation

    sealed trait Document {
      val id: Int
      type Data
    }

    /*@Resource*/ case class Email[A, B](
      @get @get override val id: Int,
      @get state: String,
      @get @post @patch subject: String,
      @put @put /*private val*/ body: A,
      @get @post @patch to: List[String],
      @get @post @patch cc: List[String] = Nil,
      @get @post @patch bcc: Option[List[String]] = None,
      @get @post var attachments: List[B] = Nil,
      @get @GET next: Option[Email[A, B]] = None
    ) extends Document {
      override type Data = A
    }
  }
}

