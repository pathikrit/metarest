package com.github.pathikrit

import org.specs2.mutable.Specification

object MetaRestSpec extends Specification {
  import com.github.pathikrit.MetaRest._

  "MetaRest" should {
    @MetaRest case class User(
      @Method(Get)                  id         : Int,
      @Method(Get, Post, Patch)     name       : String,
      @Method(Get, Post)            email      : String,
                                    createdOn  : Long
    )
    "Generate Get, Post, Patch, Put models" in {
      User.Get(0) mustBe  anInstanceOf[User.Get]
    }
  }
}
