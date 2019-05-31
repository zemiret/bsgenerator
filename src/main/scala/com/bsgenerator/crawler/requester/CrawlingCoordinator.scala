package com.bsgenerator.crawler.requester

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer, OverflowStrategy}

import scala.concurrent.duration._

object CrawlingCoordinator {
  def props: Props = Props(new CrawlingCoordinator)

  final case class HandleUrlRequest(requestId: String, url: String, respondTo: ActorRef)

  final case class DelayUrlHandlingRequest(requestId: String, url: String, respondTo: ActorRef)

  final case class Response(requestId: String, url: String, content: String)

}

class CrawlingCoordinator extends Actor with ActorLogging {
  implicit val system: ActorSystem = context.system
  implicit val materializer: Materializer = ActorMaterializer.create(system)

  protected val throttler: ActorRef = Source
    .actorRef(100000, OverflowStrategy.dropNew)
    .throttle(120, 1.minute)
    .to(Sink.actorRef(self, NotUsed))
    .run()

  protected val requestHandlersRouter: ActorRef = context.actorOf(CrawlingRouter.props())

  override def receive: Receive = waitForMessage(Map.empty)


  def waitForMessage(pendingRequestsToActor: Map[String, ActorRef]): Receive = {
    case CrawlingCoordinator.HandleUrlRequest(requestId, url, respondTo) =>
      throttler ! CrawlingCoordinator.DelayUrlHandlingRequest(requestId, url, respondTo)

    case CrawlingCoordinator.DelayUrlHandlingRequest(requestId, url, respondTo) =>
      receivedHandleUrlRequest(pendingRequestsToActor, requestId, url, respondTo)

    case CrawlingRequestHandler.Response(requestId, url, content) =>
      receivedResponse(pendingRequestsToActor, requestId, url, content)
  }

  def receivedHandleUrlRequest(
                                pendingRequestsToActor: Map[String, ActorRef],
                                requestId: String,
                                url: String,
                                respondTo: ActorRef): Unit = {

    requestHandlersRouter ! CrawlingRouter.HandleUrlRequest(requestId, url, self)
    val newPendingRequestsToActor = pendingRequestsToActor + (requestId -> respondTo)

    context become waitForMessage(newPendingRequestsToActor)
  }

  def receivedResponse(
                        pendingRequestsToActor: Map[String, ActorRef],
                        requestId: String,
                        url: String,
                        content: String): Unit = {

    if (!pendingRequestsToActor.contains(requestId)) {
      log.warning("Encountered unexpected requestId: {}", requestId)
      context become waitForMessage(pendingRequestsToActor)
    } else {
      val respondTo = pendingRequestsToActor(requestId)
      respondTo ! CrawlingCoordinator.Response(requestId, url, content)
      val newPendingRequestsToActor = pendingRequestsToActor - requestId

      context become waitForMessage(newPendingRequestsToActor)
    }
  }
}
