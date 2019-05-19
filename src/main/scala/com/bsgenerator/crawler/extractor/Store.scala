package com.bsgenerator.crawler.extractor

import akka.actor.{Actor, ActorLogging, Props}

object Store {
  def props: Props = Props(new Store)

  final case class StoreContentRequest(requestId: String, content: String)

  final case class StoreLinksRequest(requestId: String, links: Set[String])

  final case class LinksStoredResponse(requestId: String)

  final case class FilterLinksRequest(requestId: String, links: Set[String])

}

class Store extends Actor with ActorLogging {
  override def receive: Receive = Actor.emptyBehavior
}
