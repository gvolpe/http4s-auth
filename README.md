http4s-auth
===========

[ ![Codeship Status for gvolpe/http4s-auth](https://app.codeship.com/projects/213c5170-05d7-0135-edfd-52b395dcacd9/status?branch=master)](https://app.codeship.com/projects/213643)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/94bf4c355bca4354a8c2a1cda3dc40b9)](https://www.codacy.com/app/volpegabriel/http4s-auth?utm_source=github.com&utm_medium=referral&utm_content=gvolpe/http4s-auth&utm_campaign=badger)

Authentication library for [Http4s](http://http4s.org/)

## Introduction

Although Http4s [now supports](http://http4s.org/v0.15/auth/) basic token-based authentication, the use of the same feature with this library it's IMO much simpler and different. Besides it works with previous version of Http4s v0.14.x.

## Authentication Methods

#### Basic Authentication

By adding the AuthHttpEndpoint.service to the services you will have available the endpoints signup, login and logout:

```scala
object Demo extends ServerApp {

  import BindingsModule._

  override def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(AuthHttpEndpoint.service)
      .mountService(DemoHttpEndpoint.service)
      .start
  }

}
```

In order to secure an endpoint you just need to use **Secured** and have in scope implementations for TokenRepository and UserRepository:

```scala
import com.gvolpe.http4s.auth.service.Secured

object DemoHttpEndpoint {

 import com.gvolpe.http4s.auth.demo.BindingsModule._

  val service = HttpService {
    case GET -> Root / "public" =>
      Ok("Public resource")
    case req @ GET -> Root / "protected" => Secured(req) {
      Ok("Protected resource")
    }
  }

}
```

By default, InMemory representations of the repositories are available. The BindingsModule object demonstrates a recommended way of use:

```scala
import com.gvolpe.http4s.auth.repository.{InMemoryTokenRepository, InMemoryUserRepository}

object BindingsModule {

  implicit val authTokenRepository = new InMemoryTokenRepository()
  implicit val authUserRepository  = new InMemoryUserRepository()

}
```

#### Signup

- Method: POST
- JSON body: 

```json
{
  "username": "gvolpe",
  "password: "123456"
}
```
- Responses:

	- **CONFLICT 409** in case of failure (user already exists!).
	- **CREATED 201** in case of success with body including token:
	```
	{ 
	  "value": "35c07890-209d-11e7-a3b7-d13fe8119206"
	}
	```

#### Login

- Method: POST
- JSON body:

```json
{
  "username": "gvolpe",
  "password: "123456"
}
```
- Responses:

	- **200 OK** in case of success with same body as signup.
	- **401 UNAUTHORIZED** in case the password doesn't match.
	- **NOTFOUND 404** if the user does not exist.

#### Logout

- Method: POST
- Header: X-Auth-Token with the token value
- Responses:

	- **204 NoContent** in case of success.
	- **404 NotFound** in case the user does not exist.

#### Protected Endpoint

Any protected endpoint should include the **X-Auth-Token** in the request headers in order to authenticate it. Example:

```json
curl -X GET -H "X-Auth-Token: 35c07890-209d-11e7-a3b7-d13fe8119206" http://localhost:8080/protected
```

--------------------------------------------

### COMING SOON... WORK IN PROGRESS ...

#### OAuth1
#### OAuth2
#### OpenID 
#### CAS

## LICENSE

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with
the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.
