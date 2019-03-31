package com.bsgenerator.crawler

import akka.actor.ActorSystem
import akka.testkit.TestKit
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
//      TODO: How can I test/mock child? I WANT them to be CHILD of this actor, not passed via props
//      val probe = TestProbe()
//      val crawlingSupervisor = system.actorOf(CrawlingSupervisor.props(probe.ref))
//
//      crawlingSupervisor ! HandleUrl("someUrl")
//      probe.expectMsg(CrawlingBalancer.HandleUrl(_, "someUrl", crawlingSupervisor))
    }
  }
}
