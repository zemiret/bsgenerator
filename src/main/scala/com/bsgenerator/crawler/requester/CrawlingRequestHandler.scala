package com.bsgenerator.crawler.requester

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.util.{Failure, Success}


object CrawlingRequestHandler {
  def props(httpClient: HttpService): Props =
    Props(new CrawlingRequestHandler(httpClient))

  final case class HandleUrlRequest(requestId: String, url: String)

  final case class Response(requestId: String, content: String)

}

class CrawlingRequestHandler(httpClient: HttpService)
  extends Actor with ActorLogging {

  import CrawlingRequestHandler._

  implicit val actorSystem: ActorSystem = context.system
  implicit val executionContext: ExecutionContextExecutor = context.system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()


  override def receive: Receive = {
    case HandleUrlRequest(requestId, url) =>
      val _sender = sender()
      httpClient.get(url).onComplete {
        case Success(httpResponse: HttpResponse) =>
          val response = Await.result(Unmarshal(httpResponse.entity).to[String], 1.second)
          _sender ! Response(requestId, response)
        case Failure(_) => log.warning("Request to {} failed.", url)
      }
  }
}
