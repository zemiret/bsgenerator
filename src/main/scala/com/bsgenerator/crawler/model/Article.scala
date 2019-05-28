package com.bsgenerator.crawler.model

import scalikejdbc.{SQLSyntaxSupport, WrappedResultSet}

case class Article(id: Long, siteId: Long, url: String, content: String)

object Article extends SQLSyntaxSupport[Article] {
  override val tableName = "articles"

  def apply(rs: WrappedResultSet) = new Article(
    rs.long("id"),
    rs.long("siteId"),
    rs.string("url"),
    rs.string("content")
  )
}
