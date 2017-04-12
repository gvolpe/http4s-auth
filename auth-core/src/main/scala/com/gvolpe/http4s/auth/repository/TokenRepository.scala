package com.gvolpe.http4s.auth.repository

import com.gvolpe.http4s.auth.model.{HttpToken, HttpUser}

import scala.collection.mutable
import scalaz.concurrent.Task

trait TokenRepository {
  def find(token: HttpToken): Option[HttpUser]
  def save(user: HttpUser): Task[Unit]
  def remove(user: HttpUser): Task[Unit]
}

object InMemoryTokenRepository extends TokenRepository {
  private val tokens = mutable.HashMap.empty[HttpToken, HttpUser]

  override def find(token: HttpToken): Option[HttpUser] = tokens.get(token)
  override def save(user: HttpUser): Task[Unit] = Task.delay { tokens.update(user.httpToken, user) }
  override def remove(user: HttpUser): Task[Unit] = Task.delay {tokens.remove(user.httpToken) }
}