package com.gvolpe.http4s.auth.endpoint

import com.gvolpe.http4s.auth.model.{LoginForm, SignUpForm}
import com.gvolpe.http4s.auth.repository.BindingsModule._
import com.gvolpe.http4s.auth.service.AuthHttpService
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

object AuthHttpEndpoint {

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]) = jsonOf[A]
  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = jsonEncoderOf[A]

  val service = HttpService {
    case req @ POST -> Root / "signup" =>
      req.decode[SignUpForm](AuthHttpService.signUp)
    case req @ POST -> Root / "login" =>
      req.decode[LoginForm](AuthHttpService.login)
    case req @ POST -> Root / "logout" =>
      AuthHttpService.logout(req)
  }

}
