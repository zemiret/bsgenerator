package com.bsgenerator.crawler

import akka.actor.ActorSystem

import scala.io.StdIn

object BsGeneratorApp extends App {
  val system = ActorSystem("bsgenerator")

  try {
    val crawlingSupervisor = system.actorOf(
      CrawlingSupervisor.props("http://www.batey.info"),
      "crawling-supervisor")

    for (_ <- 1 to 100) {
      crawlingSupervisor ! CrawlingSupervisor.HandleUrlRequest("http://www.batey.info/akka-testing-messages-sent-to-child.html")
    }

    StdIn.readLine
  } finally {
    system.terminate
  }
}
