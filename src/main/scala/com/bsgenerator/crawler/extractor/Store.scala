package com.bsgenerator.crawler.extractor

import akka.actor.{Actor, ActorLogging, Props}
import scalikejdbc._

object Store {
  def props(baseUrl: String): Props = Props(new Store(baseUrl))

  final case class StoreContentRequest(requestId: String, content: String)

  final case class StoreLinksRequest(requestId: String, links: Set[String])

  final case class LinksStoredResponse(requestId: String)

  final case class FilterLinksRequest(requestId: String, links: Set[String])

  private def init(): Unit = {
    // TODO: Probably extract user passes to some config
    val url = "jdbc:postgresql://localhost/bsgenerator"
    val driver = "org.postgresql.Driver"
    val username = "root"
    val password = "root"

    Class.forName(driver)
    ConnectionPool.singleton(url, username, password)

    implicit val session: AutoSession.type = AutoSession
  }

  init()
}

class Store(private val baseUrl: String) extends Actor with ActorLogging {
  override def receive: Receive = Actor.emptyBehavior
}
