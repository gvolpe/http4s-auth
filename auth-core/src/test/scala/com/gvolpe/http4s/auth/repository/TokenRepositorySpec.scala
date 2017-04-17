package com.gvolpe.http4s.auth.repository

import com.gvolpe.http4s.auth.model.{HttpToken, HttpUser}
import org.scalatest.{FlatSpecLike, Matchers}

import scalaz.{-\/, \/-}

class TokenRepositorySpec extends FlatSpecLike with Matchers {

  it should "save a new token user" in {
    val repo  = new InMemoryTokenRepository()
    val user  = HttpUser("gvolpe", HttpToken("S3cr3t"))
    repo.save(user).run should be (\/-())
  }

  it should "NOT find a non existent token" in {
    val repo = new InMemoryTokenRepository()
    val user = HttpUser("gvolpe", HttpToken("S3cr3t"))
    repo.remove(user).run         shouldBe a [-\/[_]]
    repo.find(user.httpToken).run shouldBe None
  }

  it should "find a token user" in {
    val repo = new InMemoryTokenRepository()
    val user = HttpUser("gvolpe", HttpToken("S3cr3t"))
    repo.save(user).run                 should be (\/-())
    repo.find(HttpToken("S3cr3t")).run  should be (Some(user))
  }

  it should "NOT find a token user after it is deleted" in {
    val repo = new InMemoryTokenRepository()
    val user = HttpUser("gvolpe", HttpToken("S3cr3t"))
    repo.save(user).run                 should be (\/-())
    repo.remove(user).run               should be (\/-())
    repo.find(HttpToken("S3cr3t")).run  should be (None)
  }

}
