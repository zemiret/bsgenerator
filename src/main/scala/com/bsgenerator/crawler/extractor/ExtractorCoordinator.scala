package com.bsgenerator.crawler.extractor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.bsgenerator.crawler.CrawlingSupervisor
import com.bsgenerator.utils.Id

object ExtractorCoordinator {
  def props(baseUrl: String): Props = Props(new ExtractorCoordinator(baseUrl))

  final case class ExtractRequest(content: String, baseUrl: String)

  final case class ExtractedResponse(requestId: String, content: Option[String], links: Set[String])

  final case class FilteredLinksResponse(requestId: String, links: Set[String])

}

class ExtractorCoordinator(private val baseUrl: String) extends Actor with ActorLogging {

  import ExtractorCoordinator._

  protected val extractorsRouter: ActorRef = context.actorOf(ExtractorsRouter.props())

  // For now, there is only one. In case it's bottleneck, create router for it as well
  protected val store: ActorRef = context.actorOf(Store.props(baseUrl))


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

    content match {
      case Some(someContent) =>
        store ! Store.StoreContentRequest(requestId, someContent)
      case _ => log.info("Couldn't extract content, extractRequestId: {}", requestId)
    }

    if (links.nonEmpty) {
      val filterRequestId = Id.randomId()
      val newFilterRequests = filterRequests + filterRequestId

      store ! Store.FilterLinksRequest(filterRequestId, links)
      context become waitForMessage(newExtractRequests, newFilterRequests, storeLinksRequests)
    } else {
      context become waitForMessage(newExtractRequests, filterRequests, storeLinksRequests)
    }
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

    store ! Store.StoreLinksRequest(storeRequestId, links)

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
