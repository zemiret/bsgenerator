package com.bsgenerator.crawler.extractor

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.bsgenerator.crawler.CrawlingSupervisor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.reflect._


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
    "call extracting actors" in {
      // This is a very crude solution to inject a child. Simple but has some problems
      val routerHandler = TestProbe()
      val respondTo = TestProbe()
      val extractorCoordinator = TestActorRef(Props(new ExtractorCoordinator {
        override protected val extractorsRouter: ActorRef = routerHandler.ref
      }))

      extractorCoordinator ! ExtractorCoordinator.ExtractRequest("content", "baseUrl")

      routerHandler.expectMsgType(classTag[ExtractorsRouter.ExtractRequest])
    }

    "store extracted if some and call links filter" in {
      val storeHandler = TestProbe()
      val extractorCoordinator = TestActorRef(Props(new ExtractorCoordinator {
        override protected val storeManager: ActorRef = storeHandler.ref
      }))

      val links = Set("url1", "url2")
      extractorCoordinator ! ExtractorCoordinator.ExtractedResponse(
        "id",
        Option("some content"),
        links
      )

      storeHandler.expectMsgType(classTag[Store.StoreContentRequest])
      storeHandler.expectMsgType(classTag[Store.FilterLinksRequest])
    }

    "not store extracted if none and call links filter" in {
      val storeHandler = TestProbe()
      val extractorCoordinator = TestActorRef(Props(new ExtractorCoordinator {
        override protected val storeManager: ActorRef = storeHandler.ref
      }))

      val links = Set("url1", "url2")
      extractorCoordinator ! ExtractorCoordinator.ExtractedResponse(
        "id",
        Option.empty,
        links
      )

      storeHandler.expectMsgType(classTag[Store.FilterLinksRequest])
      storeHandler.expectNoMessage()
    }

    "store filtered links" in {
      val storeHandler = TestProbe()
      val extractorCoordinator = TestActorRef(Props(new ExtractorCoordinator {
        override protected val storeManager: ActorRef = storeHandler.ref
      }))

      val links = Set("url1", "url2")

      extractorCoordinator ! ExtractorCoordinator.FilteredLinksResponse("id", links)

      storeHandler.expectMsgType(classTag[Store.StoreLinksRequest])
    }

    "notify parent about new links to handle" in {
      val parent = TestProbe()
      val extractorCoordinator = TestActorRef(Props[ExtractorCoordinator], parent.ref)

      val links = Set("url1", "url2", "url3")

      extractorCoordinator ! ExtractorCoordinator.FilteredLinksResponse("id", links)

      parent.expectMsg(CrawlingSupervisor.HandleUrlRequest("url1"))
      parent.expectMsg(CrawlingSupervisor.HandleUrlRequest("url2"))
      parent.expectMsg(CrawlingSupervisor.HandleUrlRequest("url3"))
    }
  }
}
