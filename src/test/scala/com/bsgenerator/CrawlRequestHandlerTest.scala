package com.bsgenerator

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class CrawlRequestHandlerTest(_system: ActorSystem)
  extends TestKit(_system)
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

  def this() = this(ActorSystem("bsgenerator"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "crawl request handler" should {
    "respond to request" in {
      val probe = TestProbe()
      val crawlRequestHandlerActor = system.actorOf(CrawlRequestHandler.props(new MockHttpService))

      crawlRequestHandlerActor.tell(CrawlRequestHandler.HandleUrl("id1", "someurl"), probe.ref)
      probe.expectMsg(CrawlRequestHandler.Response(requestId = "id1", "mockResponse"))

      crawlRequestHandlerActor.tell(CrawlRequestHandler.HandleUrl("id2", "protocol://someurl2.kanapka.rg"), probe.ref)
      probe.expectMsg(CrawlRequestHandler.Response(requestId = "id2", "mockResponse"))
    }
  }
}