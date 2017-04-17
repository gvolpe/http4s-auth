package com.gvolpe.http4s.auth

import java.time.Clock

import org.reactormonk.{CryptoBits, PrivateKey}

import scala.util.Random

object model {

  case class HttpToken(token: String)
  case class HttpUser(username: String, httpToken: HttpToken)

  case class User(username: String, password: String)

  case class SignUpForm(username: String, password: String)
  case class LoginForm(username: String, password: String)

  object User {

    private val key    = PrivateKey(scala.io.Codec.toUTF8("Http4s-Auth-3ncrypt10n#"))
    private val crypto = CryptoBits(key)

    def encrypt(password: String): String = {
      crypto.signToken(password, Clock.systemUTC().millis.toString)
    }

    def isPasswordValid(token: String, password: String): Boolean = {
      crypto.validateSignedToken(token).contains(password)
    }

  }

  object HttpUser {

    private val key    = PrivateKey(scala.io.Codec.toUTF8("Http4s-Auth-T0k3n$"))
    private val crypto = CryptoBits(key)

    def createToken(username: String): HttpToken = {
      val clock  = Clock.systemUTC()
      val signed = crypto.signToken(username, clock.millis.toString)
      HttpToken(signed)
    }

    def validateToken(token: HttpToken): Option[HttpUser] = {
      crypto.validateSignedToken(token.token).map(HttpUser(_, token))
    }

  }

}
