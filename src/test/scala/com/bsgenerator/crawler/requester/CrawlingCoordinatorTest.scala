package com.bsgenerator.crawler.requester

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.bsgenerator.crawler.requester.CrawlingCoordinator.DelayUrlHandlingRequest
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class CrawlingCoordinatorTest(_system: ActorSystem)
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
      val crawlingBalancer = TestActorRef(Props(new CrawlingCoordinator {
        override protected val throttler: ActorRef = throttlerProbe.ref
      }))

      crawlingBalancer ! CrawlingCoordinator.HandleUrlRequest("id", "someUrl", respondTo.ref)
      throttlerProbe.expectMsg(CrawlingCoordinator.DelayUrlHandlingRequest("id", "someUrl", respondTo.ref))
    }

    "call router handler on new requests" in {
      // This is a very crude solution to inject a child. Simple but has some problems
      val routerHandler = TestProbe()
      val respondTo = TestProbe()
      val crawlingBalancer = TestActorRef(Props(new CrawlingCoordinator {
        override protected val requestHandlersRouter: ActorRef = routerHandler.ref
      }))

      crawlingBalancer ! DelayUrlHandlingRequest("id", "someUrl", respondTo.ref)
      routerHandler.expectMsg(CrawlingRouter.HandleUrlRequest("id", "someUrl", crawlingBalancer))
    }

    "re-send correct request to awaiting actor" in {
      val respondProbe = TestProbe()
      val throttleBalancer = system.actorOf(CrawlingCoordinator.props)

      throttleBalancer ! CrawlingCoordinator.DelayUrlHandlingRequest("id1", "url1", respondProbe.ref)
      throttleBalancer ! CrawlingRequestHandler.Response("id1", "url1", "content")
      respondProbe.expectMsg(CrawlingCoordinator.Response("id1", "url1", "content"))
    }

    "not send a response on incorrect requestId" in {
      val respondProbe = TestProbe()
      val throttleBalancer = system.actorOf(CrawlingCoordinator.props)

      throttleBalancer ! CrawlingCoordinator.DelayUrlHandlingRequest("id1", "url1", respondProbe.ref)
      throttleBalancer ! CrawlingRequestHandler.Response("INCORRECTID", "url1", "content")
      respondProbe.expectNoMessage()
    }
  }
}
