package com.bsgenerator.crawler.requester

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._

import scala.concurrent.Future

trait HttpService {
  def get(url: String)(implicit actorSystem: ActorSystem): Future[HttpResponse]
}

class DefaultHttpService extends HttpService {
  override def get(url: String)(implicit actorSystem: ActorSystem): Future[HttpResponse] =
    Http().singleRequest(HttpRequest(HttpMethods.GET, url))
}