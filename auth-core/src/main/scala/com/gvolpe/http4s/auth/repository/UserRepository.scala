package com.gvolpe.http4s.auth.repository

import com.gvolpe.http4s.auth.model.User

import scala.collection.mutable
import scalaz.\/
import scalaz.syntax.either._
import scalaz.concurrent.Task

trait UserRepository {
  type Username = String
  def find(username: Username): Task[Option[User]]
  def save(user: User): Task[Throwable \/ Unit]
}

class InMemoryUserRepository extends UserRepository {
  private val users  = mutable.HashMap.empty[Username, User]

  override def find(username: Username): Task[Option[User]] = Task.delay { users.get(username) }
  override def save(user: User): Task[Throwable \/ Unit] = Task.delay { users.update(user.username, user).right }
}