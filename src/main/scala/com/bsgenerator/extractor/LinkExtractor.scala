package com.bsgenerator.extractor

import org.jsoup.Jsoup

import scala.collection.JavaConverters._

class LinkExtractor() {
  def extract(content: String, context: String): Set[String] = {
    val document = Jsoup.parse(content, context)
    val linkElements = document.getElementsByTag("a").asScala.toArray
    linkElements.map(_.attr("abs:href")).toSet
  }
}
