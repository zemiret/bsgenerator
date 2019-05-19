package com.bsgenerator.crawler

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.bsgenerator.crawler.requester.CrawlingBalancer
import com.bsgenerator.utils.Id

object CrawlingSupervisor {
  def props(): Props = Props(new CrawlingSupervisor())

  final case class HandleUrlRequest(url: String)
}

class CrawlingSupervisor
  extends Actor with ActorLogging {

  protected val crawlingBalancer: ActorRef = context.actorOf(CrawlingBalancer.props)

  override def receive: Receive = waitForMessage(Set.empty)


  def waitForMessage(pendingCrawlingRequests: Set[String]): Receive = {
    case CrawlingBalancer.Response(requestId, content) =>
      receivedCrawlingResponse(pendingCrawlingRequests, requestId, content)
    case CrawlingSupervisor.HandleUrlRequest(url) =>
      receivedHandleUrl(pendingCrawlingRequests, url)
  }


  def receivedCrawlingResponse(
                                pendingCrawlingRequests: Set[String],
                                requestId: String,
                                content: String): Unit = {

    if (!pendingCrawlingRequests.contains(requestId)) {
      log.warning("Encountered unexpected requestId: {}", requestId)
      context become waitForMessage(pendingCrawlingRequests)
    } else {
      // TODO: Pass content to extractorCoordinator
      log.info("Response! {}", content)

      val newPendingCrawlingRequests = pendingCrawlingRequests - requestId

      context become waitForMessage(newPendingCrawlingRequests)
    }
  }

  def receivedHandleUrl(
                       pendingCrawlingRequests: Set[String],
                       url: String
                       ): Unit = {
    val requestId = Id.randomId()
    val newPendingCrawlingRequests = pendingCrawlingRequests + requestId

    crawlingBalancer ! CrawlingBalancer.HandleUrlRequest(requestId, url, self)

    context become waitForMessage(newPendingCrawlingRequests)
  }
}