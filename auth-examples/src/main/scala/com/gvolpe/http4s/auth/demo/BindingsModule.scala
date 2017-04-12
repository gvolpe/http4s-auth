package com.gvolpe.http4s.auth.demo

import com.gvolpe.http4s.auth.repository.{InMemoryTokenRepository, InMemoryUserRepository}

object BindingsModule {

  implicit val authTokenRepository = new InMemoryTokenRepository()
  implicit val authUserRepository  = new InMemoryUserRepository()

}
