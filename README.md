[![Build Status](https://travis-ci.org/pathikrit/metarest.png)](http://travis-ci.org/pathikrit/metarest)
[![Dependency Status](https://www.versioneye.com/user/projects/54d0f1bc3ca08495310000a2/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54d0f1bc3ca08495310000a2)

Auto generate HTTP REST request models for your business layer.

Let's say you have the following `User` model in your business layer:

```scala
case class User(id: Int, name: String, email: String, registeredOn: DateTime)
```

But, now you want to create well-formed models to describe the response/requests of your HTTP REST APIs:
```scala
// Response to GET /users/$id (Retrieve an existing user)
case class UserGet(id: Int, name: String, email: String)

// Request body of POST /users (Create a new user)
case class UserPost(name: String, email: String)

//Request body of PATCH /users/$id (Edit name of an existing user)
case class UserPatch(name: Option[String])
```

That is a lot of boilerplate! Keeping all these request models in sync with your business model and/or adding/removing fields quickly becomes difficult and cumbersome for more complicated models.
With MetaRest, all you need to do is this:

```scala
import com.github.pathikrit.MetaRest._

@MetaRest case class User(
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

Now you can have well defined repo layer:
```scala
trait UserRepo {
  def get(id: Int): User.Get
  def create(request: User.Post): User.Get
  def patch(id: Int, request: User.Patch): User.Get
}
```

TODO:
* Release to Sonatype + License
* Option to generate Play json formatters
