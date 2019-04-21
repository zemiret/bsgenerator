package com.bsgenerator.crawler.extractor

import akka.actor.{Actor, ActorLogging, Props}

object ExtractorCoordinator {
  def props: Props = Props(new ExtractorCoordinator)

  final case class Extract(content: String)
}

class ExtractorCoordinator extends Actor with ActorLogging {
  override def receive: Receive = Actor.emptyBehavior
}
