package com.bsgenerator.crawler

import akka.actor.{ActorSystem}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class CrawlingBalancerTest(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("bsgenerator"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "throttle balancer" should {
    "call handler on new requests" in {
      // TODO: This test (requires somehow accessing actorPool - probably freaking factory)
    }

    "re-send correct request to awaiting actor" in {
      val respondProbe = TestProbe()
      val throttleBalancer = system.actorOf(CrawlingBalancer.props)

      throttleBalancer ! CrawlingBalancer.HandleUrl("id1", "url1", respondProbe.ref)
      throttleBalancer ! CrawlingRequestHandler.Response("id1", "content")
      respondProbe.expectMsg(CrawlingBalancer.Response("id1", "content"))
    }

    "not send a response on incorrect requestId" in {
      val respondProbe = TestProbe()
      val throttleBalancer = system.actorOf(CrawlingBalancer.props)

      throttleBalancer ! CrawlingBalancer.HandleUrl("id1", "url1", respondProbe.ref)
      throttleBalancer ! CrawlingRequestHandler.Response("INCORRECTID", "content")
      respondProbe.expectNoMessage()
    }
  }
}
