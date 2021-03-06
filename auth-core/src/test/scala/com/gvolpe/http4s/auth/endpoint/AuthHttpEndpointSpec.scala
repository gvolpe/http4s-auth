package com.gvolpe.http4s.auth.endpoint

import com.gvolpe.http4s.auth.model._
import com.gvolpe.http4s.auth.repository.{InMemoryTokenRepository, InMemoryUserRepository, TokenRepository, UserRepository}
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl._
import org.http4s.circe._
import org.scalatest.{FlatSpecLike, Matchers}

import scalaz.{-\/, \/}
import scalaz.concurrent.Task

class AuthHttpEndpointSpec extends AuthHttpEndpointFixture {

  import com.gvolpe.http4s.auth.Http4sSpecUtils._

  it should "sign up a new user" in {
    val request   = signupRequest(SignUpForm("gvolpe", "123456"))
    val response  = authService.apply(request).run

    response.status       should be (Status.Created)
    response.bodyAsString should include ("token")
  }

  it should "give conflict when trying to sign up an existent user" in {
    val service   = authService
    val request   = signupRequest(SignUpForm("gvolpe", "123456"))
    service(request).run

    val request2  = signupRequest(SignUpForm("gvolpe", "123456"))
    val response2 = service(request2).run

    response2.status      should be (Status.Conflict)
  }

  it should "NOT logout an user if there's no user to log out" in {
    val response = authService.apply(logoutRequest).run
    response.status should be (Status.NotFound)
  }

  it should "logout an user successfully" in {
    val service   = authService
    val request   = signupRequest(SignUpForm("gvolpe", "123456"))
    val response  = service(request).run
    val token     = response.bodyAsString.drop(10).dropRight(2)

    val response2 = service(logoutRequestWithToken(token)).run
    response2.status should be (Status.NoContent)
  }

  it should "NOT login a non existent user" in {
    val response = authService.apply(loginRequest(LoginForm("gvolpe", "123456"))).run
    response.status should be (Status.NotFound)
  }

  it should "NOT login an user if the password does not match" in {
    val service   = authService
    val request   = signupRequest(SignUpForm("gvolpe", "123456"))
    service(request).run

    val response2 = service(loginRequest(LoginForm("gvolpe", "111111"))).run
    response2.status should be (Status.Unauthorized)
  }

  it should "login an user successfully" in {
    val service   = authService
    val request   = signupRequest(SignUpForm("gvolpe", "123456"))
    service(request).run

    val response2 = service(loginRequest(LoginForm("gvolpe", "123456"))).run
    response2.status        should be (Status.Ok)
    response2.bodyAsString  should include ("token")
  }

  it should "login an user successfully after it's logged out" in {
    val service   = authService
    val request   = signupRequest(SignUpForm("gvolpe", "123456"))
    val response  = service(request).run
    val token     = response.bodyAsString.drop(10).dropRight(2)

    service(logoutRequestWithToken(token)).run

    val response2 = service(loginRequest(LoginForm("gvolpe", "123456"))).run
    response2.status        should be (Status.Ok)
    response2.bodyAsString  should include ("token")
  }

  it should "FAIL to logout an user when remove token from repository fails" in {
    val service   = failureAuthService
    val request   = logoutRequestWithToken("c56d0f6e23a608cfe5f2e6922339e6b0b93c1ad8-1492469832547-gvolpe")
    val response  = service(request).run

    response.status should be (Status.InternalServerError)
  }

  it should "FAIL to login an user when save token in repository fails" in {
    val service   = failureAuthService
    val request   = loginRequest(LoginForm("mrfake", "123456"))
    val response  = service(request).run

    response.status should be (Status.InternalServerError)
  }

  it should "FAIL to signup an user when save token in repository fails" in {
    val service   = failureAuthService
    val request   = signupRequest(SignUpForm("gvolpe", "123456"))
    val response  = service(request).run

    response.status should be (Status.InternalServerError)
  }

}

object FailureTokenRepository extends TokenRepository {
  override def find(token: HttpToken): Task[Option[HttpUser]] = Task.now(Some(HttpUser("gvolpe", HttpToken("c56d0f6e23a608cfe5f2e6922339e6b0b93c1ad8-1492469832547-gvolpe"))))
  override def remove(user: HttpUser): Task[\/[Throwable, Unit]] = Task.now(-\/(new Exception("")))
  override def save(user: HttpUser): Task[\/[Throwable, Unit]] = Task.now(-\/(new Exception("")))
}

object FailureUserRepository extends UserRepository {
  val fakeDb = List(User("mrfake", User.encrypt("123456")))
  override def find(username: Username): Task[Option[User]] = Task.now { fakeDb.find(_.username == username) }
  override def save(user: User): Task[\/[Throwable, Unit]] = Task.now(-\/(new Exception("")))
}

trait AuthHttpEndpointFixture extends FlatSpecLike with Matchers {

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]) = jsonOf[A]
  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = jsonEncoderOf[A]

  def failureAuthService: HttpService = AuthHttpEndpoint.service(FailureTokenRepository, FailureUserRepository)
  def authService: HttpService = AuthHttpEndpoint.service(new InMemoryTokenRepository(), new InMemoryUserRepository())

  def signupRequest(signUpForm: SignUpForm) =
    new Request(method = POST, uri = Uri(path = "/signup"))
      .withBody(signUpForm).run

  def loginRequest(loginForm: LoginForm) =
    new Request(method = POST, uri = Uri(path = "/login"))
      .withBody(loginForm).run

  def logoutRequest =
    new Request(method = POST, uri = Uri(path = "/logout"))

  def logoutRequestWithToken(token: String) =
    new Request(method = POST, uri = Uri(path = "/logout"))
      .replaceAllHeaders(Header("x-auth-token", token))

}
