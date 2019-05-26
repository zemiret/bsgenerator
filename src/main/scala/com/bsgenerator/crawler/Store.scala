package com.bsgenerator.crawler

import akka.actor.{Actor, ActorLogging, Props}
import com.bsgenerator.crawler.model.{AllowedBase, Site, VisitedLink}
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

  def createSite(baseUrl: String, allowedBases: Set[String]): Option[Site] = {
    var site = sql"select * from sites where baseUrl = $baseUrl"
      .map(rs => Site(rs))
      .first
      .apply()

    if (site.isEmpty) {
      sql"insert into sites (baseUrl) values ($baseUrl)".update().apply()

      site = sql"select * from sites where baseUrl = $baseUrl"
        .map(rs => Site(rs))
        .first
        .apply()

      val siteId = site.get.id
      allowedBases.foreach(url =>
        sql"insert into allowedBases (url, siteId) values ($url, $siteId)".update().apply()
      )
    }

    site
  }


  def insertContent(content: String, siteId: Long) = {
    sql"insert into articles (siteId, content) values ($siteId, $content)".update().apply()
  }

  def insertLinks(links: Set[String], siteId: Long) = {
    links foreach { url =>
      sql"insert into visitedLinks (siteId, url) values ($siteId, $url)".update().apply()
    }
  }

  def getAllowedBases(siteId: Long) = {
    sql"select * from allowedBases where siteId = $siteId"
      .map(rs => AllowedBase(rs))
      .list
      .apply()
  }

  def getLinks(siteId: Long) = {
    sql"select * from visitedLinks where siteId = $siteId"
      .map(rs => VisitedLink(rs))
      .list
      .apply()
  }

  init()
}

class Store extends Actor with ActorLogging {

  import Store._

  override def receive: Receive = {
    case StoreContentRequest(content, siteId) =>
      log.info("Received store content: {}", content)
      insertContent(content, siteId)
    case FilterLinksRequest(requestId, links, siteId) =>
      val filtered = filterLinks(links, siteId)
      sender ! FilteredLinksResponse(requestId, filtered)
    case StoreLinksRequest(requestId, links, siteId) =>
      log.info("Received store links: {}", links)
      insertLinks(links, siteId)
      sender ! LinksStoredResponse(requestId)
  }

  private def filterLinks(links: Set[String], siteId: Long): Set[String] = {
    val allLinks = Store.getLinks(siteId)
      .map(visitedLink => visitedLink.url)
      .toSet

    val allowedBases = Store.getAllowedBases(siteId).map(base => base.url)
    val siteLinks = links.filter(link => allowedBases.contains(strippedLink(link)))

    siteLinks -- allLinks
  }

  private def strippedLink(link: String) = {
    val splitted = link.split("/")
    if (splitted.length >= 3) {
      splitted(2)
    } else {
      splitted(0)
    }
  }
}
