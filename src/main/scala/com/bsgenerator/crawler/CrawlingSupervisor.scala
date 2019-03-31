package com.bsgenerator.crawler

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object CrawlingSupervisor {
  def props(): Props = Props(new CrawlingSupervisor())

    final case class HandleUrl(url: String)
}

class CrawlingSupervisor()
  extends Actor with ActorLogging {

  private val crawlingBalancer = context.system.actorOf(CrawlingBalancer.props)

  override def preStart(): Unit = log.info("CrawlingSupervisor started")

  override def postStop(): Unit = log.info("CrawlingSupervisor stopped")

  override def receive: Receive = waitForMessage(Set.empty)


  def waitForMessage(pendingCrawlingRequests: Set[String]): Receive = {
    case CrawlingBalancer.Response(requestId, content) =>
      receivedCrawlingResponse(pendingCrawlingRequests, requestId, content)
    case CrawlingSupervisor.HandleUrl(url) =>
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
    val requestId = UUID.randomUUID.toString
    val newPendingCrawlingRequests = pendingCrawlingRequests + requestId

    crawlingBalancer ! CrawlingBalancer.HandleUrl(requestId, url, self)

    context become waitForMessage(newPendingCrawlingRequests)
  }
}