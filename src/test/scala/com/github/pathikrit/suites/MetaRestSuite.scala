package com.github.pathikrit.suites

import org.scalatest._, Matchers._

class MetaRestSuite extends FunSuite {
  import com.github.pathikrit.MetaRest, MetaRest._

  test("Non case classes") {
    "@MetaRest class A" shouldNot compile
    "@MetaRest trait A" shouldNot compile
    "@MetaRest case class A()" should compile
    "@MetaRest object A" shouldNot compile
  }

  todo("Complex models") {
    class GET extends scala.annotation.StaticAnnotation

    sealed trait Document {
      val id: Int
      type Data
    }

    /*@MetaRest*/ case class Email[A, B](
      @get @get override val id: Int,
      @MetaRest.get state: String,
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

