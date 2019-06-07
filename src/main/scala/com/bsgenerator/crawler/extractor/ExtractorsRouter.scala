package com.bsgenerator.crawler.extractor

import akka.actor.{Actor, ActorRef, Props}
import akka.routing.SmallestMailboxPool
import com.bsgenerator.Config

object ExtractorsRouter {
  def props(): Props = Props(new ExtractorsRouter)

  final case class ExtractRequest(RequestId: String, url: String, content: String, baseUrl: String, senderActor: ActorRef)
}

class ExtractorsRouter extends Actor {
  protected val router: ActorRef =
    context.actorOf(
      SmallestMailboxPool(Config.config.getInt("bsgenerator.extractor.poolSize"))
        .props(ExtractorFactory.createExtractor()),
      "extractorRouter"
    )

  def receive = {
    case ExtractorsRouter.ExtractRequest(requestId, url, content, baseUrl, senderActor) =>
      router.tell(Extractor.ExtractRequest(requestId, url, content, baseUrl), senderActor)
  }
}
