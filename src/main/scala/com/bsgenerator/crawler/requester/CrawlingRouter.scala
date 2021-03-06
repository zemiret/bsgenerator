package com.bsgenerator.crawler.requester

import akka.actor.{Actor, ActorRef, Props}
import akka.routing._
import com.bsgenerator.Config

object CrawlingRouter {
  def props(): Props = Props(new CrawlingRouter)

  final case class HandleUrlRequest(requestId: String, url: String, senderActor: ActorRef)
}

class CrawlingRouter extends Actor {
  protected val router: ActorRef =
    context.actorOf(
      BalancingPool(Config.config.getInt("bsgenerator.requests.poolSize"))
        .props(CrawlingRequestHandler.props(new DefaultHttpService)),
      "crawlingRouter"
    )

  def receive = {
    case CrawlingRouter.HandleUrlRequest(requestId, url, senderActor) =>
      router.tell(CrawlingRequestHandler.HandleUrlRequest(requestId, url), senderActor)
  }
}
