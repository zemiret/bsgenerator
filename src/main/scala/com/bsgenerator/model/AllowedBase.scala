package com.bsgenerator.model

import scalikejdbc.{SQLSyntaxSupport, WrappedResultSet}

case class AllowedBase(id: Long, siteId: Long, url: String)

object AllowedBase extends SQLSyntaxSupport[AllowedBase] {
  override val tableName = "allowedBases"

  def apply(rs: WrappedResultSet) = new AllowedBase(
    rs.long("id"),
    rs.long("siteId"),
    rs.string("url")
  )
}
