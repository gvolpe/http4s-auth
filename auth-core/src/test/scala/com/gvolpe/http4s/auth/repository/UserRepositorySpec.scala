package com.gvolpe.http4s.auth.repository

import com.gvolpe.http4s.auth.model.User
import org.scalatest.{FlatSpecLike, Matchers}

class UserRepositorySpec extends FlatSpecLike with Matchers {

  it should "save a new user" in {
    val repo = new InMemoryUserRepository()
    repo.save(User("gvolpe", "123456")).run should be (())
  }

  it should "NOT find a non existent user" in {
    val repo = new InMemoryUserRepository()
    repo.find("gvolpe") should be (None)
  }

  it should "find an user" in {
    val repo = new InMemoryUserRepository()
    val user = User("gvolpe", "123456")
    repo.save(user).run
    repo.find("gvolpe") should be (Some(user))
  }

}
