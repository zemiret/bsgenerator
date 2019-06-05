package com.bsgenerator.crawler.extractor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.bsgenerator.crawler.{CrawlingSupervisor, Store}
import com.bsgenerator.model.Site
import com.bsgenerator.utils.{IId, Id}

object ExtractorCoordinator {
  def props(): Props = Props(new ExtractorCoordinator)

  final case class ExtractRequest(url: String, content: String, site: Site)

  final case class ExtractedResponse(requestId: String, url: String, header: Option[String], content: Option[String], links: Set[String])


}

class ExtractorCoordinator extends Actor with ActorLogging {

  import ExtractorCoordinator._

  protected val extractorsRouter: ActorRef = context.actorOf(ExtractorsRouter.props())

  // For now, there is only one. In case it's bottleneck, create router for it as well
  protected val store: ActorRef = context.actorOf(Store.props())

  protected val idGenerator: IId = Id


  override def receive: Receive = waitForMessage(Map.empty, Map.empty, Map.empty)

  def waitForMessage(extractRequests: Map[String, Site],
                     filterRequests: Map[String, Long],
                     storeLinksRequests: Map[String, Long]): Receive = {
    case ExtractedResponse(requestId, url, header, content, links) =>
      receivedExtractedData(extractRequests, filterRequests, storeLinksRequests, requestId, url, header, content, links)
    case Store.FilteredLinksResponse(requestId, links) =>
      receivedFilteredLinks(extractRequests, filterRequests, storeLinksRequests, requestId, links)
    case ExtractRequest(url, content, site) =>
      receivedExtractRequest(extractRequests, filterRequests, storeLinksRequests, url, content, site)
    case Store.LinksStoredResponse(requestId) =>
      receivedLinksStored(extractRequests, filterRequests, storeLinksRequests, requestId)
  }

  def receivedExtractRequest(extractRequests: Map[String, Site],
                             filterRequests: Map[String, Long],
                             storeLinksRequests: Map[String, Long],
                             url: String,
                             content: String,
                             site: Site) = {

    val requestId = idGenerator.randomId()
    val newExtractRequests = extractRequests + (requestId -> site)

    extractorsRouter ! ExtractorsRouter.ExtractRequest(requestId, url, content, site.baseUrl, self)

    context become waitForMessage(
      newExtractRequests,
      filterRequests,
      storeLinksRequests
    )
  }

  def receivedExtractedData(extractRequests: Map[String, Site],
                            filterRequests: Map[String, Long],
                            storeLinksRequests: Map[String, Long],
                            requestId: String,
                            url: String,
                            header: Option[String],
                            content: Option[String],
                            links: Set[String]) = {

    val siteOption = extractRequests.get(requestId)
    var siteId: Long = -1
    val newExtractRequests = extractRequests - requestId

    siteOption match {
      case Some(someSite) =>
        siteId = someSite.id
      case _ =>
        log.warning("Invalid extract response: requestId: {}", requestId)
        context become waitForMessage(newExtractRequests, filterRequests, storeLinksRequests)
    }


    content match {
      case Some(someContent) =>
        store ! Store.StoreContentRequest(header.getOrElse(""), someContent, siteId, url)
      case _ => log.info("Couldn't extract content, extractRequestId: {}", requestId)
    }

    if (links.nonEmpty) {
      val filterRequestId = idGenerator.randomId()
      val newFilterRequests = filterRequests + (filterRequestId -> siteId)

      store ! Store.FilterLinksRequest(filterRequestId, links, siteId)
      context become waitForMessage(newExtractRequests, newFilterRequests, storeLinksRequests)
    } else {
      context become waitForMessage(newExtractRequests, filterRequests, storeLinksRequests)
    }
  }

  def receivedFilteredLinks(extractRequests: Map[String, Site],
                            filterRequests: Map[String, Long],
                            storeLinksRequests: Map[String, Long],
                            requestId: String,
                            links: Set[String]) = {


    val newFilterRequests = filterRequests - requestId

    if (links.isEmpty) {
      context become waitForMessage(extractRequests, newFilterRequests, storeLinksRequests)
    } else {
      val siteIdOption = filterRequests.get(requestId)

      siteIdOption match {
        case Some(siteId) =>
          links.foreach(link => context.parent ! CrawlingSupervisor.HandleUrlRequest(link))

          val storeRequestId = idGenerator.randomId()
          val newStoreLinksRequests = storeLinksRequests + (storeRequestId -> siteId)

          store ! Store.StoreLinksRequest(storeRequestId, links, siteId)

          context become waitForMessage(extractRequests, newFilterRequests, newStoreLinksRequests)
        case _ =>
          log.warning("Invalid filter links response: requestId: {}", requestId)
          context become waitForMessage(extractRequests, newFilterRequests, storeLinksRequests)
      }
    }
  }

  def receivedLinksStored(extractRequests: Map[String, Site],
                          filterRequests: Map[String, Long],
                          storeLinksRequests: Map[String, Long],
                          requestId: String) = {
    val newStoreLinkRequests = storeLinksRequests - requestId

    context become waitForMessage(extractRequests, filterRequests, newStoreLinkRequests)
  }
}
