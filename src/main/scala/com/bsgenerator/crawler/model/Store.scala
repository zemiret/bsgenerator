package com.bsgenerator.crawler.model

import akka.actor.{Actor, ActorLogging, Props}
import scalikejdbc._

object Store {
  def props(baseUrl: String): Props = Props(new Store(baseUrl))

  final case class StoreContentRequest(requestId: String, content: String)

  final case class StoreLinksRequest(requestId: String, links: Set[String])

  final case class LinksStoredResponse(requestId: String)

  final case class FilterLinksRequest(requestId: String, links: Set[String])

  implicit val session: AutoSession.type = AutoSession

  private def init(): Unit = {
    // TODO: Probably extract user passes to some config
    val url = "jdbc:postgresql://localhost/bsgenerator"
    val driver = "org.postgresql.Driver"
    val username = "root"
    val password = "root"

    Class.forName(driver)
    ConnectionPool.singleton(url, username, password)
  }

  def cleanup(): Unit = {
    session.close()
    ConnectionPool.closeAll()
  }

  def createSite(baseUrl: String): Option[Site] = {
    sql"insert into sites (baseUrl) values ($baseUrl)".update().apply()

    val res = sql"select * from sites where baseUrl = $baseUrl"
      .map(rs => Site(rs))
      .first
      .apply()

    res
  }

  init()
}

class Store(private val baseUrl: String) extends Actor with ActorLogging {
  override def receive: Receive = Actor.emptyBehavior
}
