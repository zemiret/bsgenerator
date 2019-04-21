package com.bsgenerator.crawler.requester

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer, OverflowStrategy}

import scala.concurrent.duration._

object CrawlingBalancer {
  def props: Props = Props(new CrawlingBalancer)

  final case class HandleUrl(requestId: String, url: String, respondTo: ActorRef)

  final case class DelayUrlHandling(requestId: String, url: String, respondTo: ActorRef)

  final case class Response(requestId: String, content: String)

}

class CrawlingBalancer extends Actor with ActorLogging {
  implicit val system: ActorSystem = context.system
  implicit val materializer: Materializer = ActorMaterializer.create(system)

  protected val throttler: ActorRef = Source
    .actorRef(10000, OverflowStrategy.dropNew)
    .throttle(20, 1.minute)
    .to(Sink.actorRef(self, NotUsed))
    .run()

  protected val requestHandlersRouter: ActorRef = context.actorOf(CrawlingBalancingRouter.props())

  override def receive: Receive = waitForMessage(Map.empty)


  def waitForMessage(pendingRequestsToActor: Map[String, ActorRef]): Receive = {
    case CrawlingBalancer.HandleUrl(requestId, url, respondTo) =>
      throttler ! CrawlingBalancer.DelayUrlHandling(requestId, url, respondTo)

    case CrawlingBalancer.DelayUrlHandling(requestId, url, respondTo) =>
      receivedHandleUrlRequest(pendingRequestsToActor, requestId, url, respondTo)

    case CrawlingRequestHandler.Response(requestId, content) =>
      receivedResponse(pendingRequestsToActor, requestId, content)
  }

  def receivedHandleUrlRequest(
                                pendingRequestsToActor: Map[String, ActorRef],
                                requestId: String,
                                url: String,
                                respondTo: ActorRef): Unit = {

    requestHandlersRouter ! CrawlingBalancingRouter.HandleUrl(requestId, url, self)
    val newPendingRequestsToActor = pendingRequestsToActor + (requestId -> respondTo)

    context become waitForMessage(newPendingRequestsToActor)
  }

  def receivedResponse(
                        pendingRequestsToActor: Map[String, ActorRef],
                        requestId: String,
                        content: String): Unit = {

    if (!pendingRequestsToActor.contains(requestId)) {
      log.warning("Encountered unexpected requestId: {}", requestId)
      context become waitForMessage(pendingRequestsToActor)
    } else {
      val respondTo = pendingRequestsToActor(requestId)
      respondTo ! CrawlingBalancer.Response(requestId, content)
      val newPendingRequestsToActor = pendingRequestsToActor - requestId

      context become waitForMessage(newPendingRequestsToActor)
    }
  }
}
