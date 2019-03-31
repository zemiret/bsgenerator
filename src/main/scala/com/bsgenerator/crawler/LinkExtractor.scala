package com.bsgenerator.crawler

import akka.actor.{Actor, ActorLogging, Props}

object LinkExtractor {
  def props: Props = Props(new LinkExtractor)
}

class LinkExtractor extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("LinkExtractor started")
  override def postStop(): Unit = log.info("LinkExtractor stopped")

  override def receive: Receive = Actor.emptyBehavior
}
