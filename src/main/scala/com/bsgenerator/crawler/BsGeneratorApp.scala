package com.bsgenerator.crawler

import akka.actor.ActorSystem

import scala.io.StdIn

object BsGeneratorApp extends App {
  val system = ActorSystem("bsgenerator")

  try {
    Store.repository.init()
    val testSite = Store.repository.createSite("https://www.fronda.pl",
      Set("www.fronda.pl")
    ).get

    val crawlingSupervisor = system.actorOf(
      CrawlingSupervisor.props(testSite),
      "crawling-supervisor")

//    crawlingSupervisor ! CrawlingSupervisor.HandleUrlRequest("http://www.batey.info/akka-testing-messages-sent-to-child.html")
    crawlingSupervisor ! CrawlingSupervisor.HandleUrlRequest("https://www.fronda.pl/a/marcin-wolski-dla-frondy-spryty-plan-w-kosiniaka-kamysza-byc-moze-to-nie-psl-a-po-przestanie-istniec,127778.html")

    StdIn.readLine
  } finally {
    system.terminate
    Store.repository.cleanup()
  }
}
