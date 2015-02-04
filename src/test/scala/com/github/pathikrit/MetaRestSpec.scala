package com.github.pathikrit

import org.specs2.mutable.Specification

object MetaRestSpec extends Specification {
  import com.github.pathikrit.MetaRest._

  "MetaRest" should {
    @MetaRest case class User(
      @Method(get)                  id            : Int,
      @Method(get, post, patch)     name          : String,
      @Method(get, post)            email         : String,
                                    registeredOn  : Long
    )
    "Generate Get, Post, Patch, Put models" in {
      User.Get(id = 0) must beAnInstanceOf[User.Get]
//      User.Post(name = "Rick", email = "pathikritbhowmick@msn.com") must beAnInstanceOf[User.Post]
//      User.Patch(name = None) must beAnInstanceOf[User.Patch]
//      User.Patch(name = Some("Pathikrit")) must beAnInstanceOf[User.Patch]
    }
  }
}
