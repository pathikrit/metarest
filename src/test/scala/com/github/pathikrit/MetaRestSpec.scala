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

      User.Get(id = 0, name = "Rick", email = "pathikritbhowmick@msn.com") must beAnInstanceOf[User.Get]
      User.Post(name = "Rick", email = "pathikritbhowmick@msn.com") must beAnInstanceOf[User.Post]
      User.Put() must beAnInstanceOf[User.Put]
      User.Patch(name = None) must beAnInstanceOf[User.Patch]
      User.Patch(name = Some("Pathikrit")) must beAnInstanceOf[User.Patch]
    }

    "Work on complex models" in {
      // Tests for complex objects (generics/dependent types,extends etc)
      // empty metarest, duplicate args to metarest, metarest.Get
      // vals and vars
      // other annotations, other annotations called Get?
      todo
    }
  }
}
