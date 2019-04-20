package com.bsgenerator.crawler.extractor

import akka.actor.{Actor, Props}
import com.bsgenerator.crawler.CrawlingSupervisor
import com.bsgenerator.utils.Helpers

object ExtractorCoordinator {
  def props: Props = Props(new ExtractorCoordinator)

  //  Messages:
  final case class Extract(content: String, baseUrl: String)

  final case class ExtractedContentAndLinks(requestId: String, content: Option[String], links: Set[String])

  final case class FilteredLinks(requestId: String, links: Set[String])
}

class ExtractorCoordinator extends Actor {

  import ExtractorCoordinator._

  override def receive: Receive = waitForMessage(Set.empty, Set.empty)

  def waitForMessage(extractRequests: Set[String],
                     filterRequests: Set[String]): Receive = {
    case ExtractedContentAndLinks(requestId, content, links) =>
      receivedExtractedData(extractRequests, filterRequests, requestId, content, links)
    case FilteredLinks(requestId, links) =>
      receivedFilteredLinks(extractRequests, filterRequests, requestId, links)
    case Extract(content, baseUrl) =>
      receivedExtractRequest(extractRequests, filterRequests, content, baseUrl)
  }

  def receivedExtractRequest(extractRequests: Set[String],
                             filterRequests: Set[String],
                             content: String,
                             baseUrl: String) = {
    // TODO: 1. Create router for extractors 2. Call router here
  }

  def receivedExtractedData(extractRequests: Set[String],
                            filterRequests: Set[String],
                            requestId: String,
                            content: Option[String],
                            links: Set[String]) = {

    // TODO: store extracted content
    // TODO: call filter links on StoreManager

    val newExtractRequests = extractRequests - requestId
    val newFilterRequests = filterRequests + Helpers.randomId()

    context become waitForMessage(newExtractRequests, newFilterRequests)
  }

  def receivedFilteredLinks(extractRequests: Set[String],
                            filterRequests: Set[String],
                            requestId: String,
                            links: Set[String]) = {

    links.foreach(link => context.parent ! CrawlingSupervisor.HandleUrl(link))

    val newFilterRequests = filterRequests - requestId
    context become waitForMessage(extractRequests, newFilterRequests)
  }
}
