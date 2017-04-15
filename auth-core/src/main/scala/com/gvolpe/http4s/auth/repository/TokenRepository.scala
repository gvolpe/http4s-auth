package com.gvolpe.http4s.auth.repository

import com.gvolpe.http4s.auth.model.{HttpToken, HttpUser}

import scala.collection.mutable
import scalaz.\/
import scalaz.syntax.either._
import scalaz.concurrent.Task

trait TokenRepository {
  def find(token: HttpToken): Task[Option[HttpUser]]
  def save(user: HttpUser): Task[Throwable \/ Unit]
  def remove(user: HttpUser): Task[Throwable \/ Unit]
}

class InMemoryTokenRepository() extends TokenRepository {
  private val tokens = mutable.HashMap.empty[HttpToken, HttpUser]

  override def find(token: HttpToken): Task[Option[HttpUser]] = Task.delay { tokens.get(token) }
  override def save(user: HttpUser): Task[Throwable \/ Unit] = Task.delay { tokens.update(user.httpToken, user).right }
  override def remove(user: HttpUser): Task[Throwable \/ Unit] = Task.delay {
    tokens.remove(user.httpToken) match {
      case Some(httpUser) => ().right
      case None           => new Exception("User not found").left
    }
  }
}