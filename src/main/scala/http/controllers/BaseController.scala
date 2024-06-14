package http.controllers

import cats.effect._

import sttp.tapir.server.ServerEndpoint

trait BaseController {

  val routes: List[ServerEndpoint[Any, IO]]

}
