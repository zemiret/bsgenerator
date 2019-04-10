package com.bsgenerator.crawler.extractor

import akka.actor.{Actor, ActorLogging, Props}

object StoreManager {
  def props: Props = Props(new StoreManager)
}

class StoreManager extends Actor with ActorLogging {
  override def receive: Receive = Actor.emptyBehavior
}
