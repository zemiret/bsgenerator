package com.bsgenerator.crawler

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.bsgenerator.crawler.extractor.ExtractorCoordinator
import com.bsgenerator.crawler.requester.CrawlingCoordinator
import com.bsgenerator.utils.Id

object CrawlingSupervisor {
  def props(baseUrl: String): Props =
    Props(new CrawlingSupervisor(baseUrl))

  final case class HandleUrlRequest(url: String)
}

class CrawlingSupervisor(private val baseUrl: String)
  extends Actor with ActorLogging {

  protected val crawlingBalancer: ActorRef = context.actorOf(CrawlingCoordinator.props)
  protected val extractorCoordinator: ActorRef = context.actorOf(ExtractorCoordinator.props(baseUrl))

  override def receive: Receive = waitForMessage(Set.empty)


  def waitForMessage(pendingCrawlingRequests: Set[String]): Receive = {
    case CrawlingCoordinator.Response(requestId, content) =>
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
      extractorCoordinator ! ExtractorCoordinator.ExtractRequest(content, baseUrl)

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

    crawlingBalancer ! CrawlingCoordinator.HandleUrlRequest(requestId, url, self)

    context become waitForMessage(newPendingCrawlingRequests)
  }
}