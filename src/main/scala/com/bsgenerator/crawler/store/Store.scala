package com.bsgenerator.crawler.store

import akka.actor.{Actor, ActorLogging, Props}

object Store {
  def props(): Props = Props(new Store)
  val repository: PostgresRepository = new PostgresRepository

  //  Akka protocol definition
  final case class StoreContentRequest(content: String, siteId: Long, url: String)

  final case class StoreLinksRequest(requestId: String, links: Set[String], siteId: Long)

  final case class LinksStoredResponse(requestId: String)

  final case class FilterLinksRequest(requestId: String, links: Set[String], siteId: Long)

  final case class FilteredLinksResponse(requestId: String, links: Set[String])
}

class Store extends Actor with ActorLogging {
  import Store._

  override def receive: Receive = {
    case StoreContentRequest(content, siteId, url) =>
      log.info("Received store content: {}", content)
      repository.insertContent(content, siteId, url)
    case FilterLinksRequest(requestId, links, siteId) =>
      val filtered = filterLinks(links, siteId)
      sender ! FilteredLinksResponse(requestId, filtered)
    case StoreLinksRequest(requestId, links, siteId) =>
      log.info("Received store links: {}", links)
      repository.insertLinks(links, siteId)
      sender ! LinksStoredResponse(requestId)
  }

  private def filterLinks(links: Set[String], siteId: Long): Set[String] = {
    val allLinks = repository.getLinks(siteId)
      .map(visitedLink => visitedLink.url)
      .toSet

    val allowedBases = repository.getAllowedBases(siteId).map(base => base.url)
    val siteLinks = links.map(trimFragment).filter(link => allowedBases.contains(strippedLink(link)))

    siteLinks -- allLinks
  }

  private def trimFragment(link: String)= link.split("#")(0)

  private def strippedLink(link: String) = {
    val splitted = link.split("/")
    if (splitted.length >= 3) {
      splitted(2)
    } else {
      splitted(0)
    }
  }

  def getRepository(): Repository = repository;
}
