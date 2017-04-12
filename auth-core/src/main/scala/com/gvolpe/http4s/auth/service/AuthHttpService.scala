package com.gvolpe.http4s.auth.service

import com.gvolpe.http4s.auth.model._
import com.gvolpe.http4s.auth.repository.{TokenRepository, UserRepository}
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

import scala.util.Random
import scalaz.concurrent.Task

object AuthHttpService {

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]) = jsonOf[A]
  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = jsonEncoderOf[A]

  private val XAuthToken = "x-auth-token"

  val unauthorized: Task[Response] = Task.now {
    Response(Status.Unauthorized)
      .withAttribute(Fallthrough.fallthroughKey, ())
      .withBody("Unauthorized.").run
  }

  def findHttpUser(headers: List[Header])(implicit tokenRepo: TokenRepository): Option[HttpUser] =
    for {
      token <- headers.find(_.name.toString() == XAuthToken)
      user  <- tokenRepo.find(HttpToken(token.value))
    } yield user

  def signUp(form: SignUpForm)(implicit tokenRepo: TokenRepository, userRepo: UserRepository): Task[Response] = {
    userRepo.find(form.username) match {
      case Some(user) =>
        Conflict(s"User with username ${user.username} already exists!")
      case None =>
        for {
          _        <- userRepo.save(User(form.username, form.password)) // TODO: encrypt password
          token    = HttpUser.createToken
          _        <- tokenRepo.save(HttpUser(form.username, 1L, token)) // TODO: Expiry key
          response <- Created(token)
        } yield response
    }
  }

  def logout(req: Request)(implicit tokenRepo: TokenRepository): Task[Response] = {
    findHttpUser(req.headers.toList) match {
      case Some(user) =>
        tokenRepo.remove(user).flatMap(_ => NoContent())
      case None =>
        NotFound()
    }
  }

  def login(form: LoginForm)(implicit tokenRepo: TokenRepository, userRepo: UserRepository): Task[Response] = {
    userRepo.find(form.username) match {
      case Some(user) if user.password == form.password =>
        val token = HttpUser.createToken
        val user  = HttpUser(Random.nextLong().toString, 1L, token) // TODO: Expiry key
        tokenRepo.save(user).flatMap(_ => Ok(token))
      case Some(user) =>
        unauthorized
      case None =>
        NotFound(s"Username ${form.username} not found!")
    }
  }

}

object Secured {
  def apply(req: Request)(response: Task[Response])(implicit repo: TokenRepository): Task[Response] = {
    AuthHttpService.findHttpUser(req.headers.toList) match {
      case Some(u) => response
      case None    => AuthHttpService.unauthorized
    }
  }
}