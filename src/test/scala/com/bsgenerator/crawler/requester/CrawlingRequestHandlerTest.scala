package com.bsgenerator.crawler.requester

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class CrawlingRequestHandlerTest(_system: ActorSystem)
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
      val crawlRequestHandlerActor = system.actorOf(CrawlingRequestHandler.props(new MockHttpService))

      crawlRequestHandlerActor.tell(CrawlingRequestHandler.HandleUrlRequest("id1", "someurl"), probe.ref)
      probe.expectMsg(CrawlingRequestHandler.Response(requestId = "id1", "someurl", "mockResponse"))

      crawlRequestHandlerActor.tell(CrawlingRequestHandler.HandleUrlRequest("id2", "protocol://someurl2.kanapka.rg"), probe.ref)
      probe.expectMsg(CrawlingRequestHandler.Response(requestId = "id2", "protocol://someurl2.kanapka.rg", "mockResponse"))
    }
  }
}