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
import org.slf4j.LoggerFactory

import scalaz.{-\/, \/-}
import scalaz.EitherT.eitherT
import scalaz.concurrent.Task

object AuthHttpService {

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]) = jsonOf[A]
  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = jsonEncoderOf[A]

  private val XAuthToken = "x-auth-token"

  private val log = LoggerFactory.getLogger(getClass)

  val unauthorized: Task[Response] = Response(Status.Unauthorized).withBody("Unauthorized.")

  def findTokenFromHeaders(headers: List[Header]): Option[HttpUser] =
    headers.find(_.name.toString() == XAuthToken) flatMap { tokenHeader =>
      HttpUser.validateToken(HttpToken(tokenHeader.value))
    }

  def findHttpUser(headers: List[Header])(implicit tokenRepo: TokenRepository): Task[Option[HttpUser]] = {
    findTokenFromHeaders(headers) match {
      case Some(user) =>
        tokenRepo.find(user.httpToken).map(_.filter(_ == user))
      case None =>
        Task.now(None)
    }
  }

  def signUp(form: SignUpForm)(implicit tokenRepo: TokenRepository, userRepo: UserRepository): Task[Response] = {
    userRepo.find(form.username) flatMap {
      case Some(user) =>
        Conflict(s"User with username ${user.username} already exists!")
      case None =>
        val token = HttpUser.createToken(form.username)
        (for {
          _ <- eitherT(userRepo.save(User(form.username, User.encrypt(form.password))))
          _ <- eitherT(tokenRepo.save(HttpUser(form.username, token)))
        } yield ()).run flatMap {
          case \/-(()) =>
//            val expires = Some(Instant.now().plus(1, ChronoUnit.DAYS))
            Created(token) //.addCookie(Cookie(XAuthToken, token.token, expires))
          case -\/(error) =>
            InternalServerError(error.getMessage)
        }
    }
  }

  def logout(req: Request)(implicit tokenRepo: TokenRepository): Task[Response] = {
    findHttpUser(req.headers.toList) flatMap {
      case Some(user) =>
        tokenRepo.remove(user) flatMap {
          case \/-(())    =>
            NoContent() //.removeCookie(XAuthToken)
          case -\/(error) =>
            log.info(s"Logout: ${error.getMessage}")
            InternalServerError(error.getMessage)
        }
      case None =>
        NotFound()
    }
  }

  def login(form: LoginForm)(implicit tokenRepo: TokenRepository, userRepo: UserRepository): Task[Response] = {
    userRepo.find(form.username) flatMap {
      case Some(user) if User.isPasswordValid(user.password, form.password) =>
        val token = HttpUser.createToken(form.username)
        val user  = HttpUser(form.username, token)
        tokenRepo.save(user).flatMap {
          case \/-(())    =>
//            val expires = Some(Instant.now().plus(1, ChronoUnit.DAYS))
            Ok(token) //.addCookie(Cookie(XAuthToken, token.token, expires))
          case -\/(error) =>
            log.info(s"Logout: ${error.getMessage}")
            InternalServerError(error.getMessage)
        }
      case Some(user) =>
        unauthorized
      case None =>
        NotFound(s"Username ${form.username} not found!")
    }
  }

}

object Secured {
  def apply(req: Request)(response: Task[Response])(implicit repo: TokenRepository): Task[Response] =
    AuthHttpService.findHttpUser(req.headers.toList) flatMap {
      case Some(u) => response
      case None    => AuthHttpService.unauthorized
    }
}