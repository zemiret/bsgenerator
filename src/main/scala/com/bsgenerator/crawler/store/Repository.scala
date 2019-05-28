package com.bsgenerator.crawler.store

import com.bsgenerator.crawler.model.{AllowedBase, Site, VisitedLink}

trait Repository {

  def init();

  def cleanup();

  def createSite(baseUrl: String, allowedBases: Set[String]): Option[Site];

  def insertContent(content: String, siteId: Long, url: String);

  def insertLinks(links: Set[String], siteId: Long);

  def getAllowedBases(siteId: Long): List[AllowedBase];

  def getLinks(siteId: Long): List[VisitedLink];
}
