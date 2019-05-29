package com.bsgenerator.crawler.requester

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._

import scala.concurrent.{ExecutionContext, Future}

trait HttpService {
  def get(url: String)(implicit actorSystem: ActorSystem): Future[HttpResponse]
}

class DefaultHttpService extends HttpService {
  val maxRedirectCount = 20

  override def get(url: String)(implicit actorSystem: ActorSystem): Future[HttpResponse] = get(HttpRequest(HttpMethods.GET, url), 0)

  def get(req: HttpRequest, recursionDepth: Int = 0)(implicit actorSystem: ActorSystem): Future[HttpResponse] = {
    implicit val ec: ExecutionContext = actorSystem.dispatcher
    Http().singleRequest(req).flatMap { resp =>
      resp.status match {
        case StatusCodes.Found => resp.header[headers.Location].map { loc =>
          val newRequest = req.copy(uri = req.uri.copy(scheme = loc.uri.scheme, authority = loc.uri.authority))
          if (recursionDepth < maxRedirectCount) get(newRequest, recursionDepth + 1) else Http().singleRequest(newRequest)
        }.getOrElse(Future(resp))
        case _ => Future(resp)
      }
    }
  }
}
