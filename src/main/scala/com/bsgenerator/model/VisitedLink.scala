package com.bsgenerator.model

import scalikejdbc.{SQLSyntaxSupport, WrappedResultSet}

case class VisitedLink(id: Long, siteId: Long, url: String)

object VisitedLink extends SQLSyntaxSupport[VisitedLink] {
  override val tableName = "visitedLinks"

  def apply(rs: WrappedResultSet) = new VisitedLink(
    rs.long("id"), rs.long("siteId"), rs.string("url")
  )
}
