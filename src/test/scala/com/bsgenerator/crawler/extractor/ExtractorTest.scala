package com.bsgenerator.crawler.extractor

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import mocks.extractor.{ArticleExtractorMock, HeaderExtractorMock, LinkExtractorMock}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


class ExtractorTest(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("bsgenerator"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "extractor" should {
    "extract data and return to sender" in {
      val senderProbe = TestProbe()
      val links = Set("url1", "url2")

      val extractor = system.actorOf(
        Extractor.props(
          new HeaderExtractorMock("header"),
          new ArticleExtractorMock("content"),
          new LinkExtractorMock(links)
        ))

      extractor.tell(Extractor.ExtractRequest("id", "url", "content", "baseUrl"), senderProbe.ref)

      senderProbe.expectMsg(
        ExtractorCoordinator.ExtractedResponse("id", "url", Option("header"), Option("content"), links)
      )
    }
  }
}


