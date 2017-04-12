package com.gvolpe.http4s.auth

import com.gvolpe.http4s.auth.endpoint.{AuthHttpEndpoint, DemoHttpEndpoint}
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.{Server, ServerApp}

import scalaz.concurrent.Task

object Demo extends ServerApp {

  override def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(AuthHttpEndpoint.service)
      .mountService(DemoHttpEndpoint.publicResource)
      .mountService(DemoHttpEndpoint.protectedResource)
      .start
  }

}
