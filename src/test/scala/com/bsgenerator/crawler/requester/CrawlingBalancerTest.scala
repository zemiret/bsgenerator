package com.bsgenerator.crawler.requester

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.bsgenerator.crawler.requester.CrawlingBalancer.{DelayUrlHandling, HandleUrl}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.immutable

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
    "add message to the throttled queue when it comes" in {
      val throttlerProbe = TestProbe()
      val respondTo = TestProbe()
      val crawlingBalancer = TestActorRef(Props(new CrawlingBalancer {
        override protected val throttler: ActorRef = throttlerProbe.ref
      }))

      crawlingBalancer ! CrawlingBalancer.HandleUrl("id", "someUrl", respondTo.ref)
      throttlerProbe.expectMsg(CrawlingBalancer.DelayUrlHandling("id", "someUrl", respondTo.ref))
    }

    "call handler on new requests" in {
      // This is a very crude solution to inject a child. Simple but has some problems
      val childHandlerProbe = TestProbe()
      val respondTo = TestProbe()
      val crawlingBalancer = TestActorRef(Props(new CrawlingBalancer {
        override protected val handlerPool: immutable.IndexedSeq[ActorRef] =
          (1 to 10) map { _ => childHandlerProbe.ref }
      }))

      crawlingBalancer ! DelayUrlHandling("id", "someUrl", respondTo.ref)
      childHandlerProbe.expectMsg(CrawlingRequestHandler.HandleUrl("id", "someUrl"))
    }

    "re-send correct request to awaiting actor" in {
      val respondProbe = TestProbe()
      val throttleBalancer = system.actorOf(CrawlingBalancer.props)

      throttleBalancer ! CrawlingBalancer.DelayUrlHandling("id1", "url1", respondProbe.ref)
      throttleBalancer ! CrawlingRequestHandler.Response("id1", "content")
      respondProbe.expectMsg(CrawlingBalancer.Response("id1", "content"))
    }

    "not send a response on incorrect requestId" in {
      val respondProbe = TestProbe()
      val throttleBalancer = system.actorOf(CrawlingBalancer.props)

      throttleBalancer ! CrawlingBalancer.DelayUrlHandling("id1", "url1", respondProbe.ref)
      throttleBalancer ! CrawlingRequestHandler.Response("INCORRECTID", "content")
      respondProbe.expectNoMessage()
    }
  }
}
