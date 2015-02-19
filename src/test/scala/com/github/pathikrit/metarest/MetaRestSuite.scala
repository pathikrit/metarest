package com.github.pathikrit.metarest

import org.scalatest._, Matchers._

class MetaRestSuite extends FunSuite {
  import com.github.pathikrit.metarest.annotations._
  import play.api.libs.json.{Json, Reads, Writes}

  def testJsonRoundTrip[A: Reads : Writes](model: A) = Json.parse(Json.toJson(model).toString()).as[A] shouldEqual model

  test("Generation of Get, Post, Patch, Put models with JSON capabilities") {
    @Resource case class User(
      @get                id            : Int,
      @get @post @patch   name          : String,
      @get @post          email         : String,
                          registeredOn  : Long
    )

    testJsonRoundTrip(User.Get(id = 0, name = "Rick", email = "awesome@msn.com"))
    testJsonRoundTrip(User.Post(name = "Rick", email = "awesome@msn.com"))
    "User.Put()" shouldNot compile
    testJsonRoundTrip(User.Patch(name = Some("Pathikrit")))
    "User.Patch()" should compile
  }

  test("Non case classes") {
    "@Resource class A" shouldNot compile
    "@Resource trait A" shouldNot compile
    "@Resource case class A()" should compile
    "@Resource object A" shouldNot compile
  }

  todo("Complex models") {
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

