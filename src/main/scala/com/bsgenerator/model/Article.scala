package com.bsgenerator.model

import scalikejdbc.{SQLSyntaxSupport, WrappedResultSet}

case class Article(id: Long, siteId: Long, url: String, header: String, content: String)

object Article extends SQLSyntaxSupport[Article] {
  override val tableName = "articles"

  def apply(rs: WrappedResultSet) = new Article(
    rs.long("id"),
    rs.long("siteId"),
    rs.string("url"),
    rs.string("header"),
    rs.string("content")
  )
}
