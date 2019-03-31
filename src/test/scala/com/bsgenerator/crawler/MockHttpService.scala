package com.bsgenerator.crawler
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}

import scala.concurrent.Future

class MockHttpService extends HttpService {
  override def get(url: String)(implicit actorSystem: ActorSystem): Future[HttpResponse] =
    Future.successful(
      HttpResponse(
        StatusCodes.OK,
        entity = "mockResponse"
      )
    )
}
