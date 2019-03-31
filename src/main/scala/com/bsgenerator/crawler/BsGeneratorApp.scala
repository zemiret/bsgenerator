package com.bsgenerator.crawler

import akka.actor.ActorSystem

import scala.io.StdIn

object BsGeneratorApp extends App {
  val system = ActorSystem("bsgenerator")

  try {
    val crawlingSupervisor = system.actorOf(
     CrawlingSupervisor.props,
      "crawling-supervisor")

    crawlingSupervisor ! CrawlingSupervisor.HandleUrl("http://www.batey.info/akka-testing-messages-sent-to-child.html")

    StdIn.readLine
  } finally {
    system.terminate
  }
}
