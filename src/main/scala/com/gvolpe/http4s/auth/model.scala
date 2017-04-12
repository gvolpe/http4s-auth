package com.gvolpe.http4s.auth

import com.gilt.timeuuid.TimeUuid

object model {

  case class HttpToken(token: String)
  case class HttpUser(username: String, timestamp: Long, httpToken: HttpToken)

  case class User(username: String, password: String)

  case class SignUpForm(username: String, password: String)
  case class LoginForm(username: String, password: String)

  object HttpUser {
    def createToken: HttpToken = {
      val token = TimeUuid().toString
      println(token)
      HttpToken(token)
    }
  }

}
