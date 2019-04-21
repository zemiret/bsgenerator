package com.bsgenerator.crawler.extractor

import akka.actor.{Actor, ActorLogging, Props}

object StoreManager {
  def props: Props = Props(new StoreManager)

  final case class StoreContent(requestId: String, content: String)

  final case class StoreLinks(requestId: String, links: Set[String])

  final case class LinksStored(requestId: String)

  final case class FilterPresentLinks(requestId: String, links: Set[String])
}

class StoreManager extends Actor with ActorLogging {
  override def receive: Receive = Actor.emptyBehavior
}
