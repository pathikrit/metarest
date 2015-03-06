MetaRest [![Build Status](https://travis-ci.org/pathikrit/metarest.png?branch=master)](http://travis-ci.org/pathikrit/metarest) [![Download](https://api.bintray.com/packages/pathikrit/maven/metarest/images/download.svg)](https://bintray.com/pathikrit/maven/metarest/_latestVersion)
--------
Use Scala macros to generate your RESTy models

Let's say you have the following `User` model in your business layer:
```scala
case class User(id: Int, name: String, email: String, registeredOn: DateTime)
```

But, now you want to create well-formed models to describe the requests/responses of your HTTP REST APIs:
```scala
// Response to GET /users/$id (Retrieve an existing user)
case class UserGet(id: Int, name: String, email: String)

// Request body of POST /users (Create a new user)
case class UserPost(name: String, email: String)

//Request body of PATCH /users/$id (Edit name of an existing user)
case class UserPatch(name: Option[String])
```

That is a lot of boilerplate! Keeping all these request models in sync with your business model and/or adding/removing fields quickly becomes tedious for more complicated models.
With MetaRest, all you need to do is:
```scala
import com.github.pathikrit.metarest.annotations._

@Resource case class User(
  @get               id            : Int,
  @get @post @patch  name          : String,
  @get @post         email         : String,
                     registeredOn  : DateTime
)
```

The above block would generate code essentially looking like this:
```scala
object User {
  case class Get(id: Int, name: String, email: String)
  case class Post(name: String, email: String)
  case class Patch(name: Option[String])
}
```

Now, you can have a well defined CRUD interface for your API:
```scala
trait UserRepo {
  def get(id: Int): User.Get
  def create(request: User.Post): User.Get
  def update(id: Int, request: User.Patch): User.Get
}
```

**JSON Support**

MetaRest can automatically generate various JSON formatters:

To use [Play's JSON](https://www.playframework.com/documentation/2.4.x/ScalaJson) formatters use the `@ResourceWithPlayJson` annotation:
```scala
import com.github.pathikrit.metarest.annotations.{ResourceWithPlayJson => Resource,
                                                  get, put, post, patch}
@Resource case class User(
  @get               id            : Int,
  @get @post @patch  name          : String,
  @get @post         email         : String,
                     registeredOn  : DateTime
)

import play.api.libs.json.Json

val jsonStr: String = """{
  "name": "Rick",
  "email": "awesome@msn.com"
}"""

val request: User.Post = Json.parse(jsonStr).as[User.Post]
val json: JsValue = Json.toJson(request)

println(s"REQUEST=$request", s"JSON=$json")
assert(json.toString == jsonStr)
```

You can similarly use [Spray's JSON](https://github.com/spray/spray-json) formatters by using the `@ResourceWithSprayJson` annotation instead:
```scala
import com.github.pathikrit.metarest.annotations.{ResourceWithSprayJson => Resource,
                                                  get, put, post, patch}
@Resource case class User(
  @get               id            : Int,
  @get @post @patch  name          : String,
  @get @post         email         : String,
                     registeredOn  : DateTime
)

import spray.json._, DefaultJsonProtocol._

val jsonStr: String = """{
  "name": "Rick",
  "email": "awesome@msn.com"
}"""

val request: User.Post = jsonStr.parseJson.convertTo[User.Post]
val json: JsValue = request.toJson

println(s"REQUEST=$request", s"JSON=$json")
assert(json.prettyPrint == jsonStr)
```

**sbt**

In your `build.sbt`, add the following entries:
```scala
resolvers += Resolver.bintrayRepo("pathikrit", "maven")

libraryDependencies += "com.github.pathikrit" %% "metarest" % "1.0.0"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
```

If you are using the `@ResourceWithPlayJson` annotation, you may need to add the following:
```scala
libraryDependencies ++= Seq(
  "com.kifi" %% "json-annotation" % "0.2",
  "com.typesafe.play" %% "play-json" % "2.3.8" // No need to add this if you are already using Play 2.1+
)
```

If you are using `@ResourceWithSprayJson` annotation, you may need to add the following:
```scala
resolvers += "bleibinha.us/archiva releases" at "http://bleibinha.us/archiva/repository/releases"

libraryDependencies ++= Seq(
  "us.bleibinha" %% "spray-json-annotation" % "0.4",
  "io.spray" %% "spray-json" % "1.3.1",  // No need to add this if you are already using Spray
)
```

Although this library currently only supports Scala 2.11+, [older versions](https://github.com/pathikrit/metarest/tree/a883c674c67a31f9eddf70797328e864f185a714) of this library that used to support Scala 2.10.x are available [here](http://dl.bintray.com/pathikrit/maven/com/github/pathikrit).
