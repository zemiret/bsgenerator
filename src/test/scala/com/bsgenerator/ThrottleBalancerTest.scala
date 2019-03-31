package com.bsgenerator

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ThrottleBalancerTest(_system: ActorSystem)
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
      // TODO: This test (requires somehow accessing actorPool)
    }

    "re-send correct request to awaiting actor" in {
      val respondProbe = TestProbe()
      val throttleBalancer = system.actorOf(ThrottleBalancer.props)

      throttleBalancer ! ThrottleBalancer.HandleUrl("id1", "url1", respondProbe.ref)
      throttleBalancer ! CrawlRequestHandler.Response("id1", "content")
      respondProbe.expectMsg(ThrottleBalancer.Response("id1", "content"))
    }

    "not send a response on incorrect requestId" in {
      val respondProbe = TestProbe()
      val throttleBalancer = system.actorOf(ThrottleBalancer.props)

      throttleBalancer ! ThrottleBalancer.HandleUrl("id1", "url1", respondProbe.ref)
      throttleBalancer ! CrawlRequestHandler.Response("INCORRECTID", "content")
      respondProbe.expectNoMessage()
    }
  }
}
