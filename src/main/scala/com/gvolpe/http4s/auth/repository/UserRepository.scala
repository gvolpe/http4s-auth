package com.gvolpe.http4s.auth.repository

import com.gvolpe.http4s.auth.model.User

import scala.collection.mutable
import scalaz.concurrent.Task

trait UserRepository {
  type Username = String
  def find(username: Username): Option[User]
  def save(user: User): Task[Unit]
}

object InMemoryUserRepository extends UserRepository {
  private val users  = mutable.HashMap.empty[Username, User]

  override def find(username: Username): Option[User] = users.get(username)
  override def save(user: User): Task[Unit] = Task.delay { users.update(user.username, user) }
}