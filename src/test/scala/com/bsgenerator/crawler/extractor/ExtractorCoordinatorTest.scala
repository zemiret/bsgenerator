package com.bsgenerator.crawler.extractor

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.bsgenerator.crawler.model.Site
import com.bsgenerator.crawler.{CrawlingSupervisor, Store}
import com.bsgenerator.utils.IId
import mocks.extractor.MockId
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.reflect._


class ExtractorCoordinatorTest(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("bsgenerator"))

  val testsite = new Site(123, "baseUrl")

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

      extractorCoordinator ! ExtractorCoordinator.ExtractRequest("content", testsite)

      routerHandler.expectMsgType(classTag[ExtractorsRouter.ExtractRequest])
    }

    "store extracted if some and call links filter" in {
      val storeHandler = TestProbe()
      val extractorCoordinator = TestActorRef(Props(new ExtractorCoordinator {
        override protected val store: ActorRef = storeHandler.ref
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

    "not store extracted and call links filter if none" in {
      val storeHandler = TestProbe()
      val extractorCoordinator = TestActorRef(Props(new ExtractorCoordinator {
        override protected val store: ActorRef = storeHandler.ref
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

    "not store filtered links if requestId is malformed" in {
      val storeHandler = TestProbe()
      val extractorCoordinator = TestActorRef(Props(new ExtractorCoordinator {
        override protected val store: ActorRef = storeHandler.ref
      }))

      val links = Set("url1", "url2")

      extractorCoordinator ! Store.FilteredLinksResponse("id", links)
      storeHandler.expectNoMessage()
    }

    "store filtered links when request is matched" in {
      val storeHandler = TestProbe()
      val extractRequest = Map(MockId.id -> testsite)
      val filterRequests = Map(MockId.id -> testsite.id)
      val storeLinkRequests = Map(MockId.id -> testsite.id)

      val extractorCoordinator = TestActorRef(Props(new ExtractorCoordinator {
        override protected val store: ActorRef = storeHandler.ref
        override protected val idGenerator: IId = MockId

        override def receive: Receive =
          super.waitForMessage(extractRequest, filterRequests, storeLinkRequests)
      }))

      val links = Set("url1", "url2")

      extractorCoordinator ! Store.FilteredLinksResponse(MockId.id, links)

      storeHandler.expectMsg(Store.StoreLinksRequest(MockId.id, links, testsite.id))
    }

    "notify parent about new links to handle" in {
      val parent = TestProbe()

      val extractRequest = Map(MockId.id -> testsite)
      val filterRequests = Map(MockId.id -> testsite.id)
      val storeLinkRequests = Map(MockId.id -> testsite.id)

      val extractorCoordinator = TestActorRef(Props(new ExtractorCoordinator {
        override protected val idGenerator: IId = MockId

        override def receive: Receive =
          super.waitForMessage(extractRequest, filterRequests, storeLinkRequests)
      }), parent.ref)

      val links = Set("url1", "url2", "url3")

      extractorCoordinator ! Store.FilteredLinksResponse(MockId.id, links)

      parent.expectMsg(CrawlingSupervisor.HandleUrlRequest("url1"))
      parent.expectMsg(CrawlingSupervisor.HandleUrlRequest("url2"))
      parent.expectMsg(CrawlingSupervisor.HandleUrlRequest("url3"))
    }
  }
}
