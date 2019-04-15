package com.bsgenerator.crawler.requester

import akka.actor.{Actor, ActorRef, Props}
import akka.routing._

object CrawlingBalancingRouter {
  def props(): Props = Props(new CrawlingBalancingRouter)

  final case class HandleUrl(requestId: String, url: String, senderActor: ActorRef)
}

class CrawlingBalancingRouter extends Actor {
  protected val router: ActorRef =
    context.actorOf(
      BalancingPool(30).props(CrawlingRequestHandler.props(new DefaultHttpService)),
      "crawlingBalancerRouter"
    )

  def receive = {
    case CrawlingBalancingRouter.HandleUrl(requestId, url, senderActor) =>
      router.tell(CrawlingRequestHandler.HandleUrl(requestId, url), senderActor)
  }
}