package com.gvolpe.http4s.auth.service

import com.gvolpe.http4s.auth.model.{HttpToken, HttpUser}
import com.gvolpe.http4s.auth.repository.InMemoryTokenRepository
import org.http4s._
import org.http4s.dsl._
import org.scalatest.{FlatSpecLike, Matchers}

class AuthHttpServiceSpec extends AuthHttpServiceFixture {

  it should "authorized a request with a valid token" in {
    implicit val repo = new InMemoryTokenRepository()

    val token = HttpToken("S3cr3t")
    val user  = HttpUser("gvolpe", 1L, token)

    repo.save(user).run

    val request   = requestWithToken(token.token)
    val response  = Secured(request)(Ok("")).run

    response.status should be (Status.Ok)
  }

  it should "NOT authorized a request without a valid token" in {
    implicit val repo = new InMemoryTokenRepository()

    val token = HttpToken("S3cr3t")
    val user  = HttpUser("gvolpe", 1L, token)

    repo.save(user).run

    val request   = Request(uri = Uri(path = "/foo"))
    val response  = Secured(request)(Ok("")).run

    response.status should be (Status.Unauthorized)
  }

  it should "find a token user from headers" in {
    implicit val repo = new InMemoryTokenRepository()

    val token = HttpToken("S3cr3t")
    val user  = HttpUser("gvolpe", 1L, token)

    repo.save(user).run

    val result = AuthHttpService.findHttpUser(List(Header("x-auth-token", token.token)))
    result should be (Some(user))
  }

  it should "NOT find a token user from headers if it was not persisted" in {
    implicit val authTokenRepository = new InMemoryTokenRepository()

    val result = AuthHttpService.findHttpUser(List(Header("x-auth-token", "S3cr3t")))
    result should be (None)
  }

  it should "NOT find a token user from headers if X-Auth-Token header is not part of it" in {
    implicit val authTokenRepository = new InMemoryTokenRepository()

    val result = AuthHttpService.findHttpUser(List.empty[Header])
    result should be (None)
  }

}

trait AuthHttpServiceFixture extends FlatSpecLike with Matchers {

  def requestWithToken(token: String) =
    Request(uri = Uri(path = "/foo"))
      .replaceAllHeaders(Header("x-auth-token", token))

}
