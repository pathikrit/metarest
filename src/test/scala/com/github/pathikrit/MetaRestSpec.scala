package com.github.pathikrit

import org.specs2.mutable.Specification

object MetaRestSpec extends Specification {
  import com.github.pathikrit.MetaRest._

  "MetaRest" should {

    "Generate Get, Post, Patch, Put models" in {
      @MetaRest case class User(
        @get                id            : Int,
        @get @post @patch   name          : String,
        @get @post          email         : String,
                            registeredOn  : Long
      )

      User.Get(id = 0, name = "Rick", email = "awesome@msn.com") must beAnInstanceOf[User.Get]
      User.Post(name = "Rick", email = "awesome@msn.com") must beAnInstanceOf[User.Post]
      User.Put() must beAnInstanceOf[User.Put]
      User.Patch(name = None) must beAnInstanceOf[User.Patch]
      User.Patch(name = Some("Pathikrit")) must beAnInstanceOf[User.Patch]
    }

    "Work on complex models" in {
      /*
      sealed trait Document {
        val id: Int
        type Data
      }

      @MetaRest case class Email[A, B](
        @get   override val id             : Int,
        @get @post @patch   subject        : String,
        @put @put           body           : A,
        @get @post @patch   to             : List[String],
        @get @post @patch   cc             : List[String] = Nil,
        @get @post @patch   bcc            : Option[List[String]] = None,
        @get @post      var attachments    : List[B] = Nil
      ) extends Document {
        override type Data = A
      }

      Email.Get(id = 0, subject = "test", to = "me") must beAnInstanceOf[Email.Get]
      */
      todo //TODO: empty metarest, metarest.Get, other annotations, other annotations called Get?
    }
  }
}
