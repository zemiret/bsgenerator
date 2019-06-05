package com.bsgenerator.crawler.extractor

import akka.actor.{Actor, ActorRef, Props}
import akka.routing.SmallestMailboxPool
import com.bsgenerator.Config
import com.bsgenerator.extractor.article.HeuristicExtractor
import com.bsgenerator.extractor.header.OpenGraphTitleExtractor
import com.bsgenerator.extractor.link.AttributeLinkExtractor

object ExtractorsRouter {
  def props(): Props = Props(new ExtractorsRouter)

  final case class ExtractRequest(RequestId: String, url: String, content: String, baseUrl: String, senderActor: ActorRef)

}

class ExtractorsRouter extends Actor {
  protected val router: ActorRef =
    context.actorOf(
      SmallestMailboxPool(Config.config.getInt("bsgenerator.extractor.poolSize"))
        .props(Extractor.props(new OpenGraphTitleExtractor, new HeuristicExtractor, new AttributeLinkExtractor)),
      "extractorRouter"
    )

  def receive = {
    case ExtractorsRouter.ExtractRequest(requestId, url, content, baseUrl, senderActor) =>
      router.tell(Extractor.ExtractRequest(requestId, url, content, baseUrl), senderActor)
  }
}
