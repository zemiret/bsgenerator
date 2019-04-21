package com.bsgenerator.crawler.extractor

import akka.actor.{Actor, ActorRef, Props}
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

  protected val extractorsRouter: ActorRef = context.actorOf(ExtractorsRouter.props())

  // For now, there is only one. In case it's bottleneck, create router for it as well
  protected val storeManager: ActorRef = context.actorOf(StoreManager.props)


  override def receive: Receive = waitForMessage(Set.empty, Set.empty, Set.empty)

  def waitForMessage(extractRequests: Set[String],
                     filterRequests: Set[String],
                     storeLinksRequests: Set[String]): Receive = {
    case ExtractedContentAndLinks(requestId, content, links) =>
      receivedExtractedData(extractRequests, filterRequests, storeLinksRequests, requestId, content, links)
    case FilteredLinks(requestId, links) =>
      receivedFilteredLinks(extractRequests, filterRequests, storeLinksRequests, requestId, links)
    case Extract(content, baseUrl) =>
      receivedExtractRequest(extractRequests, filterRequests, storeLinksRequests, content, baseUrl)
    case StoreManager.LinksStored(requestId) =>
      receivedLinksStored(extractRequests, filterRequests, storeLinksRequests, requestId)
  }

  def receivedExtractRequest(extractRequests: Set[String],
                             filterRequests: Set[String],
                             storeLinksRequests: Set[String],
                             content: String,
                             baseUrl: String) = {

    val requestId = Helpers.randomId()
    val newExtractRequests = extractRequests + requestId

    extractorsRouter ! ExtractorsRouter.Extract(requestId, content, baseUrl, self)

    context become waitForMessage(
      newExtractRequests,
      filterRequests,
      storeLinksRequests
    )
  }

  def receivedExtractedData(extractRequests: Set[String],
                            filterRequests: Set[String],
                            storeLinksRequests: Set[String],
                            requestId: String,
                            content: Option[String],
                            links: Set[String]) = {
    val newExtractRequests = extractRequests - requestId

    val filterRequestId = Helpers.randomId()
    val newFilterRequests = filterRequests + filterRequestId

    content match {
      case Some(contentString) =>
        storeManager ! StoreManager.StoreContent(requestId, contentString)
      case _ =>
    }
    storeManager ! StoreManager.FilterPresentLinks(filterRequestId, links)

    context become waitForMessage(newExtractRequests, newFilterRequests, storeLinksRequests)
  }

  def receivedFilteredLinks(extractRequests: Set[String],
                            filterRequests: Set[String],
                            storeLinksRequests: Set[String],
                            requestId: String,
                            links: Set[String]) = {

    links.foreach(link => context.parent ! CrawlingSupervisor.HandleUrl(link))
    val newFilterRequests = filterRequests - requestId

    val storeRequestId = Helpers.randomId()
    val newStoreLinksRequests = storeLinksRequests + storeRequestId

    storeManager ! StoreManager.StoreLinks(storeRequestId, links)

    context become waitForMessage(extractRequests, newFilterRequests, newStoreLinksRequests)
  }

  def receivedLinksStored(extractRequests: Set[String],
                          filterRequests: Set[String],
                          storeLinksRequests: Set[String],
                          requestId: String) = {
    val newStoreLinkRequests = storeLinksRequests - requestId

    context become waitForMessage(extractRequests, filterRequests, newStoreLinkRequests)
  }
}
