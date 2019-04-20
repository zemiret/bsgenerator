package com.bsgenerator.crawler.extractor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.bsgenerator.crawler.CrawlingSupervisor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ExtractorCoordinatorTest(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("bsgenerator"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "extractor coordinator" should {
    "notify parent about new links to handle" in {
      val parent = TestProbe()
      val extractorCoordinator = TestActorRef(Props[ExtractorCoordinator], parent.ref)

      val links = Set("url1", "url2", "url3")

      extractorCoordinator ! ExtractorCoordinator.FilteredLinks("id", links)

      parent.expectMsg(CrawlingSupervisor.HandleUrl("url1"))
      parent.expectMsg(CrawlingSupervisor.HandleUrl("url2"))
      parent.expectMsg(CrawlingSupervisor.HandleUrl("url3"))
    }

    "store extracted data and call links filter" in {
      // reminder to fill the test
      assert(false == true)
    }

    "call extracting actors" in {
      // reminder to fill the test
      assert(false == true)
    }
  }
}
