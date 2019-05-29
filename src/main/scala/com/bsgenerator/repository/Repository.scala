package com.bsgenerator.repository

import com.bsgenerator.model.{AllowedBase, Article, Site, VisitedLink}

trait Repository {

  def init();

  def cleanup();

  def createSite(baseUrl: String, allowedBases: Set[String]): Option[Site];

  def insertContent(header: String, content: String, siteId: Long, url: String);

  def insertLinks(links: Set[String], siteId: Long);

  def getAllowedBases(siteId: Long): List[AllowedBase];

  def getLinks(siteId: Long): List[VisitedLink];

  def getArticles(): List[Article];
}
