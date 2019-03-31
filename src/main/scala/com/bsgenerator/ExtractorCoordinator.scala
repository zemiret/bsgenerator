package com.bsgenerator

import akka.actor.{Actor, ActorLogging, Props}

object ExtractorCoordinator {
  def props: Props = Props(new ExtractorCoordinator)
}

class ExtractorCoordinator extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("ExtractorCoordinator started")
  override def postStop(): Unit = log.info("ExtractorCoordinator stopped")

  override def receive: Receive = Actor.emptyBehavior
}
