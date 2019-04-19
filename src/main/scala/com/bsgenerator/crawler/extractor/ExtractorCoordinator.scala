package com.bsgenerator.crawler.extractor

import akka.actor.{Actor, Props}

object ExtractorCoordinator {
  def props: Props = Props(new ExtractorCoordinator)

  final case class Extract(content: String, baseUrl: String)

  final case class ExtractedContent(requestId: String, content: String)

  final case class ExtractedLinks(requestId: String, links: Set[String])

  final case class FilteredLinks(requestId: String, links: Set[String])

}

class ExtractorCoordinator extends Actor {
  override def receive: Receive = Actor.emptyBehavior
}
