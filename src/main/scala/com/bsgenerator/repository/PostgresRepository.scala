package com.bsgenerator.repository

import com.bsgenerator.model.{AllowedBase, Article, Site, VisitedLink}
import scalikejdbc._

class PostgresRepository extends Repository {
  implicit val session: AutoSession.type = AutoSession

  //  Init db connection. Remember to call cleanup when finishing program
  def init(): Unit = {
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

  def createSite(baseUrl: String, allowedBases: Set[String]) = {
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

  def insertContent(header: String, content: String, siteId: Long, url: String) = {
    sql"insert into articles (siteId, url, header, content) values ($siteId, $url, $header, $content)".update().apply()
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

  def getArticles() = {
    sql"select * from articles"
      .map(rs => Article(rs))
      .list
      .apply()
  }
}
