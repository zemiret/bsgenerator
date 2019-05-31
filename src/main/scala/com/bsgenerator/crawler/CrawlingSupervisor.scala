package com.bsgenerator.crawler

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.bsgenerator.crawler.extractor.ExtractorCoordinator
import com.bsgenerator.model.Site
import com.bsgenerator.crawler.requester.CrawlingCoordinator
import com.bsgenerator.utils.{IId, Id}

object CrawlingSupervisor {
  def props(site: Site): Props =
    Props(new CrawlingSupervisor(site))

  final case class HandleUrlRequest(url: String)
}

class CrawlingSupervisor(private val site: Site)
  extends Actor with ActorLogging {

  protected val crawlingBalancer: ActorRef = context.actorOf(CrawlingCoordinator.props)
  protected val extractorCoordinator: ActorRef = context.actorOf(ExtractorCoordinator.props())

  protected val idGenerator: IId = Id

  override def receive: Receive = waitForMessage(Set.empty)


  def waitForMessage(pendingCrawlingRequests: Set[String]): Receive = {
    case CrawlingCoordinator.Response(requestId, url, content) =>
      receivedCrawlingResponse(pendingCrawlingRequests, requestId, content, url)
    case CrawlingSupervisor.HandleUrlRequest(url) =>
      receivedHandleUrl(pendingCrawlingRequests, url)
  }


  def receivedCrawlingResponse(
                                pendingCrawlingRequests: Set[String],
                                requestId: String,
                                content: String,
                                url: String): Unit = {

    if (!pendingCrawlingRequests.contains(requestId)) {
      log.warning("Encountered unexpected requestId: {}", requestId)
      context become waitForMessage(pendingCrawlingRequests)
    } else {
      extractorCoordinator ! ExtractorCoordinator.ExtractRequest(url, content, site)

      val newPendingCrawlingRequests = pendingCrawlingRequests - requestId

      context become waitForMessage(newPendingCrawlingRequests)
    }
  }

  def receivedHandleUrl(
                       pendingCrawlingRequests: Set[String],
                       url: String
                       ): Unit = {
    val requestId = idGenerator.randomId()
    val newPendingCrawlingRequests = pendingCrawlingRequests + requestId

    crawlingBalancer ! CrawlingCoordinator.HandleUrlRequest(requestId, url, self)

    context become waitForMessage(newPendingCrawlingRequests)
  }
}