package com.bsgenerator.extractor.header

import org.jsoup.Jsoup

class OpenGraphTitleExtractor extends HeaderExtractor {
  override def extract(content: String): Option[String] = {
    val soup = Jsoup.parse(content)
    val ogTitle = soup.selectFirst("meta[property=\"og:title\"]")
    if (ogTitle != null) {
      return Option(ogTitle.attr("content"))
    }
    val titleElement = soup.getElementsByTag("title")
    if (!titleElement.isEmpty) {
      return Option(titleElement.first().text())
    }
    Option.empty
  }
}
