package com.gvolpe.http4s.auth.repository

import com.gvolpe.http4s.auth.model.{HttpToken, HttpUser}

import scala.collection.mutable

trait TokenRepository {
  def find(token: HttpToken): Option[HttpUser]
  def save(user: HttpUser): Unit
  def remove(user: HttpUser): Unit
}

object InMemoryTokenRepository extends TokenRepository {
  private val tokens = mutable.HashMap.empty[HttpToken, HttpUser]

  // TODO: Make the Unit operations async using Task
  override def find(token: HttpToken): Option[HttpUser] = tokens.get(token)
  override def save(user: HttpUser): Unit = tokens.update(user.httpToken, user)
  override def remove(user: HttpUser): Unit = tokens.remove(user.httpToken)
}