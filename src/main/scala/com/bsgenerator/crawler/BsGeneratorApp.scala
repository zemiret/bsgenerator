package com.bsgenerator.crawler

import akka.actor.ActorSystem

import scala.io.StdIn

object BsGeneratorApp extends App {
  val system = ActorSystem("bsgenerator")

  try {

    val testSite = Store.createSite("http://www.batey.info",
      Set("www.batey.info")
    ).get

    val crawlingSupervisor = system.actorOf(
      CrawlingSupervisor.props(testSite),
      "crawling-supervisor")


    for (_ <- 1 to 1) {
      crawlingSupervisor ! CrawlingSupervisor.HandleUrlRequest("http://www.batey.info/akka-testing-messages-sent-to-child.html")
    }

    StdIn.readLine
  } finally {
    system.terminate
    Store.cleanup()
  }
}
