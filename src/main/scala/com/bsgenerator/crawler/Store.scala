package com.bsgenerator.crawler

import akka.actor.{Actor, ActorLogging, Props}
import com.bsgenerator.crawler.model.{Site, VisitedLink}
import scalikejdbc._

object Store {
  def props(): Props = Props(new Store)

  //  Akka protocol definition
  final case class StoreContentRequest(content: String, siteId: Long)

  final case class StoreLinksRequest(requestId: String, links: Set[String], siteId: Long)

  final case class LinksStoredResponse(requestId: String)

  final case class FilterLinksRequest(requestId: String, links: Set[String], siteId: Long)

  final case class FilteredLinksResponse(requestId: String, links: Set[String])

  implicit val session: AutoSession.type = AutoSession

  //  Init db connection. Remember to call cleanup when finishing program
  private def init(): Unit = {
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

    sql"select * from sites where baseUrl = $baseUrl"
      .map(rs => Site(rs))
      .first
      .apply()
  }

  def insertContent(content: String, siteId: Long): SQL[Nothing, NoExtractor] = {
    sql"insert into articles (siteId, content) values ($siteId, $content)"
  }

  def getLinks(siteId: Long) = {
    sql"select * from visitedLinks where siteId = $siteId"
      .map(rs => VisitedLink(rs))
      .list
      .apply()
  }

  def insertLinks(links: Set[String], siteId: Long) = {
    links foreach { url =>
      sql"insert into visitedLinks (siteId, url) values ($siteId, $url)"
    }
  }

  init()
}

class Store extends Actor with ActorLogging {

  import Store._

  override def receive: Receive = {
    case StoreContentRequest(content, siteId) =>
      insertContent(content, siteId)
    case FilterLinksRequest(requestId, links, siteId) =>
      val filtered = filterLinks(links, siteId)
      sender ! FilteredLinksResponse(requestId, filtered)
    case StoreLinksRequest(requestId, links, siteId) =>
      insertLinks(links, siteId)
      sender ! LinksStoredResponse(requestId)
  }

  private def filterLinks(links: Set[String], siteId: Long): Set[String] = {
    val allLinks = Store.getLinks(siteId)
      .map(visitedLink => visitedLink.url)
      .toSet
    links -- allLinks
  }
}
