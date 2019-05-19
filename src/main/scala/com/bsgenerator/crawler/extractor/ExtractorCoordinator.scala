package com.bsgenerator.crawler.extractor

import akka.actor.{Actor, ActorRef, Props}
import com.bsgenerator.crawler.CrawlingSupervisor
import com.bsgenerator.utils.Id

object ExtractorCoordinator {
  def props: Props = Props(new ExtractorCoordinator)

  final case class ExtractRequest(content: String, baseUrl: String)

  final case class ExtractedResponse(requestId: String, content: Option[String], links: Set[String])

  final case class FilteredLinksResponse(requestId: String, links: Set[String])

}

class ExtractorCoordinator extends Actor {

  import ExtractorCoordinator._

  protected val extractorsRouter: ActorRef = context.actorOf(ExtractorsRouter.props())

  // For now, there is only one. In case it's bottleneck, create router for it as well
  protected val storeManager: ActorRef = context.actorOf(Store.props)


  override def receive: Receive = waitForMessage(Set.empty, Set.empty, Set.empty)

  def waitForMessage(extractRequests: Set[String],
                     filterRequests: Set[String],
                     storeLinksRequests: Set[String]): Receive = {
    case ExtractedResponse(requestId, content, links) =>
      receivedExtractedData(extractRequests, filterRequests, storeLinksRequests, requestId, content, links)
    case FilteredLinksResponse(requestId, links) =>
      receivedFilteredLinks(extractRequests, filterRequests, storeLinksRequests, requestId, links)
    case ExtractRequest(content, baseUrl) =>
      receivedExtractRequest(extractRequests, filterRequests, storeLinksRequests, content, baseUrl)
    case Store.LinksStoredResponse(requestId) =>
      receivedLinksStored(extractRequests, filterRequests, storeLinksRequests, requestId)
  }

  def receivedExtractRequest(extractRequests: Set[String],
                             filterRequests: Set[String],
                             storeLinksRequests: Set[String],
                             content: String,
                             baseUrl: String) = {

    val requestId = Id.randomId()
    val newExtractRequests = extractRequests + requestId

    extractorsRouter ! ExtractorsRouter.ExtractRequest(requestId, content, baseUrl, self)

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

    val filterRequestId = Id.randomId()
    val newFilterRequests = filterRequests + filterRequestId

    content match {
      case Some(contentString) =>
        storeManager ! Store.StoreContentRequest(requestId, contentString)
      case _ =>
    }
    storeManager ! Store.FilterLinksRequest(filterRequestId, links)

    context become waitForMessage(newExtractRequests, newFilterRequests, storeLinksRequests)
  }

  def receivedFilteredLinks(extractRequests: Set[String],
                            filterRequests: Set[String],
                            storeLinksRequests: Set[String],
                            requestId: String,
                            links: Set[String]) = {

    links.foreach(link => context.parent ! CrawlingSupervisor.HandleUrlRequest(link))
    val newFilterRequests = filterRequests - requestId

    val storeRequestId = Id.randomId()
    val newStoreLinksRequests = storeLinksRequests + storeRequestId

    storeManager ! Store.StoreLinksRequest(storeRequestId, links)

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
