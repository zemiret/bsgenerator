package com.bsgenerator

import akka.actor.{Actor, ActorLogging, Props}

object CrawlingSupervisor {
  def props: Props = Props(new CrawlingSupervisor)
}

class CrawlingSupervisor extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("CrawlingSupervisor started")
  override def postStop(): Unit = log.info("CrawlingSupervisor stopped")

  override def receive: Receive = Actor.emptyBehavior
}