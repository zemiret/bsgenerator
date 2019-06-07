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

    crawlingSupervisor ! CrawlingSupervisor.HandleUrlRequest("https://www.fronda.pl/a/matka-kurka-wy-tuska-sie-nie-bojta-to-dziurawy-balon,127945.html")

    StdIn.readLine
  } finally {
    system.terminate
    Store.repository.cleanup()
  }
}
