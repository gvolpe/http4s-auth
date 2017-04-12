package com.gvolpe.http4s.auth.demo

import com.gvolpe.http4s.auth.repository.{InMemoryTokenRepository, InMemoryUserRepository}

object BindingsModule {

  implicit val authTokenRepository = InMemoryTokenRepository
  implicit val authUserRepository  = InMemoryUserRepository

}
