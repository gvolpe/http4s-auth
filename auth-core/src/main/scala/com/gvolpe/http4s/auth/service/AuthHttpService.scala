package com.gvolpe.http4s.auth.service

import java.time.Instant
import java.time.temporal.ChronoUnit

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
import scalaz.{-\/, \/-}
import scalaz.EitherT.eitherT
import scalaz.concurrent.Task

object AuthHttpService {

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]) = jsonOf[A]
  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = jsonEncoderOf[A]

  private val XAuthToken = "x-auth-token"

  val unauthorized: Task[Response] =
    Response(Status.Unauthorized)
      .withAttribute(Fallthrough.fallthroughKey, ())
      .withBody("Unauthorized.")

  // TODO: Also valid Cookie
  def findHttpUser(headers: List[Header])(implicit tokenRepo: TokenRepository): Task[Option[HttpUser]] =
    headers.find(_.name.toString() == XAuthToken) match {
      case Some(token) =>
        tokenRepo.find(HttpToken(token.value))
      case None =>
        Task.now(None)
    }

  def signUp(form: SignUpForm)(implicit tokenRepo: TokenRepository, userRepo: UserRepository): Task[Response] = {
    userRepo.find(form.username) flatMap {
      case Some(user) =>
        Conflict(s"User with username ${user.username} already exists!")
      case None =>
        val token = HttpUser.createToken
        (for {
          _ <- eitherT(userRepo.save(User(form.username, form.password))) // TODO: encrypt password
          _ <- eitherT(tokenRepo.save(HttpUser(form.username, 1L, token))) // TODO: Expiry key
        } yield ()).run flatMap {
          case \/-(()) =>
            val expires = Some(Instant.now().plus(1, ChronoUnit.DAYS))
            Created(token).addCookie(Cookie(XAuthToken, token.token, expires))
          case -\/(error) =>
            InternalServerError(error.getMessage)
        }
    }
  }

  def logout(req: Request)(implicit tokenRepo: TokenRepository): Task[Response] = {
    findHttpUser(req.headers.toList) flatMap {
      case Some(user) =>
        tokenRepo.remove(user) flatMap {
          case \/-(())    => NoContent().removeCookie(XAuthToken)
          case -\/(error) => InternalServerError(error.getMessage) // TODO: Log error
        }
      case None =>
        NotFound()
    }
  }

  def login(form: LoginForm)(implicit tokenRepo: TokenRepository, userRepo: UserRepository): Task[Response] = {
    userRepo.find(form.username) flatMap {
      case Some(user) if user.password == form.password =>
        val token = HttpUser.createToken
        val user  = HttpUser(Random.nextLong().toString, 1L, token) // TODO: Expiry key
        tokenRepo.save(user).flatMap {
          case \/-(())    =>
            val expires = Some(Instant.now().plus(1, ChronoUnit.DAYS))
            Ok(token).addCookie(Cookie(XAuthToken, token.token, expires))
          case -\/(error) => InternalServerError(error.getMessage) // TODO: Log error
        }
      case Some(user) =>
        unauthorized
      case None =>
        NotFound(s"Username ${form.username} not found!")
    }
  }

}

object Secured {
  def apply(req: Request)(response: Task[Response])(implicit repo: TokenRepository): Task[Response] = {
    AuthHttpService.findHttpUser(req.headers.toList) flatMap {
      case Some(u) => response
      case None    => AuthHttpService.unauthorized
    }
  }
}