package com.bsgenerator

import akka.actor.{Actor, ActorLogging, Props}

object StoreManager {
  def props: Props = Props(new StoreManager)
}

class StoreManager extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("StoreManager started")
  override def postStop(): Unit = log.info("StoreManager stopped")

  override def receive: Receive = Actor.emptyBehavior
}
