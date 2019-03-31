package com.bsgenerator

import akka.actor.ActorSystem

import scala.io.StdIn

object BsGeneratorApp extends App {
  val system = ActorSystem("bsgenerator")

  try {
    val crawlingSupervisor =
      system.actorOf(CrawlingSupervisor.props, "crawling-supervisor")

    StdIn.readLine
  } finally {
    system.terminate
  }
}