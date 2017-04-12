MetaRest [![CircleCI](https://img.shields.io/circleci/project/pathikrit/metarest.svg)](https://circleci.com/gh/pathikrit/metarest) [![Download](https://api.bintray.com/packages/pathikrit/maven/metarest/images/download.svg)](https://bintray.com/pathikrit/maven/metarest/_latestVersion)
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
import com.github.pathikrit.metarest._

@Resource case class User(
  @get               id            : Int,
  @get @post @patch  name          : String,
  @get @post         email         : String,
                     registeredOn  : DateTime
)
```

The above annotated code would generate code essentially looking like this:
```scala
object User {
  case class Get(id: Int, name: String, email: String)
  case class Post(name: String, email: String)
  case class Patch(name: Option[String])        // Note: all fields in a PATCH are optional
}
```

Now, you can have a well defined CRUD interface for your API:
```scala
trait UserRepo {
  def getAll: List[User.Get]
  def get(id: Int): User.Get
  def create(request: User.Post): User.Get
  def replace(id: Int, request: User.Put): User.Get
  def update(id: Int, request: User.Patch): User.Get
  def delete(id: Int): User.Get
}
```

**sbt**

In your `build.sbt`, add the following entries:
```scala
resolvers += Resolver.bintrayRepo("pathikrit", "maven")

libraryDependencies += "com.github.pathikrit" %% "metarest" % "1.0.0"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
```

Although this library currently only supports Scala 2.11+, [older versions](https://github.com/pathikrit/metarest/tree/a883c674c67a31f9eddf70797328e864f185a714) of this library that used to support Scala 2.10.x are available [here](http://dl.bintray.com/pathikrit/maven/com/github/pathikrit).
