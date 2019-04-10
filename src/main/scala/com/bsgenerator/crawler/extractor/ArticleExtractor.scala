package com.bsgenerator.crawler.extractor

import akka.actor.{Actor, ActorLogging, Props}

object ArticleExtractor {
  def props: Props = Props(new ArticleExtractor)
}

class ArticleExtractor extends Actor with ActorLogging {
  override def receive: Receive = Actor.emptyBehavior
}
