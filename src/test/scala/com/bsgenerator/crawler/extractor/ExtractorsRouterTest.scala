package com.bsgenerator.crawler.extractor

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


class ExtractorsRouterTest(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("bsgenerator"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "extractors router" should {
    "route work to children" in {
      val routerProbe = TestProbe()
      val senderProbe = TestProbe()

      val router = TestActorRef(Props(new ExtractorsRouter {
        override protected val router: ActorRef = routerProbe.ref
      }))

      router ! ExtractorsRouter.ExtractRequest("id", "url", "content", "base", senderProbe.ref)
      routerProbe.expectMsg(Extractor.ExtractRequest("id", "url", "content", "base"))
    }
  }
}


