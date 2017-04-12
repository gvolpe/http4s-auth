package com.gvolpe.http4s.auth.repository

import com.gvolpe.http4s.auth.model.{HttpToken, HttpUser}
import org.scalatest.{FlatSpecLike, Matchers}

class TokenRepositorySpec extends FlatSpecLike with Matchers {

  it should "save a new token user" in {
    val repo  = new InMemoryTokenRepository()
    val user  = HttpUser("gvolpe", 1L, HttpToken("S3cr3t"))
    repo.save(user).run should be (())
  }

  it should "NOT find a non existent token" in {
    val repo = new InMemoryTokenRepository()
    repo.find(HttpToken("S3cr3t")) should be (None)
  }

  it should "find a token user" in {
    val repo = new InMemoryTokenRepository()
    val user = HttpUser("gvolpe", 1L, HttpToken("S3cr3t"))
    repo.save(user).run            should be (())
    repo.find(HttpToken("S3cr3t")) should be (Some(user))
  }

  it should "NOT find a token user after it is deleted" in {
    val repo = new InMemoryTokenRepository()
    val user = HttpUser("gvolpe", 1L, HttpToken("S3cr3t"))
    repo.save(user).run             should be (())
    repo.remove(user).run           should be (())
    repo.find(HttpToken("S3cr3t"))  should be (None)
  }

}
