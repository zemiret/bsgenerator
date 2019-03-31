package com.bsgenerator.crawler

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

object CrawlingBalancer {
  def props: Props = Props(new CrawlingBalancer)

  final case class HandleUrl(requestId: String, url: String, respondTo: ActorRef)

  final case class Response(requestId: String, content: String)

}

class CrawlingBalancer extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("ThrottleBalancer started")

  override def postStop(): Unit = log.info("ThrottleBalancer stopped")

  implicit val system: ActorSystem = context.system
  private val actorPool = (1 to 10) map { _ => system.actorOf(CrawlingRequestHandler.props(new DefaultHttpService)) }


  override def receive: Receive = waitForMessage(Map.empty)


  def waitForMessage(pendingRequestsToActor: Map[String, ActorRef]): Receive = {
    case CrawlingBalancer.HandleUrl(requestId, url, respondTo) =>
      // TODO: Balance it with throttling (use streams maybe?)
      receivedHandleUrlRequest(pendingRequestsToActor, requestId, url, respondTo)
    case CrawlingRequestHandler.Response(requestId, content) =>
      receivedResponse(pendingRequestsToActor, requestId, content)
  }

  def receivedHandleUrlRequest(
                                pendingRequestsToActor: Map[String, ActorRef],
                                requestId: String,
                                url: String,
                                respondTo: ActorRef): Unit = {
    // TODO: For now, for each request an actor is chosen at random (random scheduler xD)!! This should be done with load balancer and actor pool

    val requestHandler = actorPool(util.Random.nextInt(actorPool.size))
    requestHandler ! CrawlingRequestHandler.HandleUrl(requestId, url)
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
