package com.bsgenerator.crawler

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.bsgenerator.crawler.CrawlingSupervisor.HandleUrlRequest
import com.bsgenerator.crawler.requester.CrawlingBalancer
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class CrawlingSupervisorTest(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("bsgenerator"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "crawling supervisor" should {
    "delegate url handling to balancer" in {
      // This is a very crude solution to inject a child. There are better ways!
      val probe = TestProbe()
      val crawlingSupervisor = TestActorRef(Props(new CrawlingSupervisor("baseUrl") {
        override protected val crawlingBalancer: ActorRef = probe.ref
      }))

      crawlingSupervisor ! HandleUrlRequest("someUrl")
      probe.expectMsgClass(classOf[CrawlingBalancer.HandleUrlRequest])
    }
  }
}
