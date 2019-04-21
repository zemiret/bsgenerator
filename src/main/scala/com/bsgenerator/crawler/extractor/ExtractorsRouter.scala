package com.bsgenerator.crawler.extractor

import akka.actor.{Actor, ActorRef, Props}
import akka.routing.BalancingPool
import com.bsgenerator.extractor.article.HeuristicExtractor
import com.bsgenerator.extractor.link.AttributeLinkExtractor

object ExtractorsRouter {
  def props(): Props = Props(new ExtractorsRouter)

  final case class Extract(RequestId: String, content: String, baseUrl: String, senderActor: ActorRef)

}

class ExtractorsRouter extends Actor {
  protected val router: ActorRef =
    context.actorOf(
      BalancingPool(30).props(Extractor.props(new HeuristicExtractor, new AttributeLinkExtractor)),
      "extractorRouter"
    )

  def receive = {
    case ExtractorsRouter.Extract(requestId, content, baseUrl, senderActor) =>
      router.tell(Extractor.Extract(requestId, content, baseUrl), senderActor)
  }
}
