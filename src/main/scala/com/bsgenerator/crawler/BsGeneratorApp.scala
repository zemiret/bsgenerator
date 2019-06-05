package com.bsgenerator.crawler

import akka.actor.ActorSystem

import scala.io.StdIn

object BsGeneratorApp extends App {
  val system = ActorSystem("bsgenerator")

  try {
    Store.repository.init()
    val testSite = Store.repository.createSite("http://wiadomosci.gazeta.pl",
      Set("www.wiadomosci.gazeta.pl", "wiadomosci.gazeta.pl")
    ).get

    val crawlingSupervisor = system.actorOf(
      CrawlingSupervisor.props(testSite),
      "crawling-supervisor")

    crawlingSupervisor ! CrawlingSupervisor.HandleUrlRequest("http://wiadomosci.gazeta.pl/wiadomosci/14,166794,24864924.html#s=BoxOpImg1")

    StdIn.readLine
  } finally {
    system.terminate
    Store.repository.cleanup()
  }
}
