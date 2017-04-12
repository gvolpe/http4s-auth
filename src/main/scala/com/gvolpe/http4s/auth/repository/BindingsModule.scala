package com.gvolpe.http4s.auth.repository

object BindingsModule {

  implicit val authTokenRepository = InMemoryTokenRepository
  implicit val authUserRepository  = InMemoryUserRepository

}
