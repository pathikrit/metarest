package com.github.pathikrit.metarest

import org.scalatest._, Matchers._

class MetaRestSuite extends FunSuite {
  test("Generation of Get, Post, Patch, Put models") {
    import com.github.pathikrit.metarest.annotations.{Resource, get, put, post, patch}

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

  todo("Complex models") {
    import com.github.pathikrit.metarest.annotations._

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

  private[this] def todo(msg: String)(f: => Unit) = println(s"TODO: $msg")
}

