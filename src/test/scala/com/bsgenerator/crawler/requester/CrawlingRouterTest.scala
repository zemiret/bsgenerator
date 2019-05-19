package com.bsgenerator.crawler.requester

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


class CrawlingRouterTest(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("bsgenerator"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "router balancer" should {
    "router work to children" in {
      val routerProbe = TestProbe()
      val senderProbe = TestProbe()

      val router = TestActorRef(Props(new CrawlingRouter {
        override protected val router: ActorRef = routerProbe.ref
      }))

      router ! CrawlingRouter.HandleUrlRequest("id", "someUrl", senderProbe.ref)
      routerProbe.expectMsg(CrawlingRequestHandler.HandleUrlRequest("id", "someUrl"))
    }
  }
}

