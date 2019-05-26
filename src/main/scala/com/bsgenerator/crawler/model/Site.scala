package com.bsgenerator.crawler.model

import scalikejdbc.{SQLSyntaxSupport, WrappedResultSet}


case class Site(id: Long, baseUrl: String)

object Site extends SQLSyntaxSupport[Site] {

  override val tableName = "sites"

  def apply(rs: WrappedResultSet) = new Site(
    rs.long("id"), rs.string("baseUrl")
  )
}