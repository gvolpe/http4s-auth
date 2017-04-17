package com.gvolpe.http4s.auth

import org.http4s.Response

object Http4sSpecUtils {

  implicit class ResponseOps(response: Response) {
    def bodyAsString: String = response.bodyAsText.runLast.run.getOrElse("empty body")
  }

}
