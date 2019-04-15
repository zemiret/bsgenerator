package com.bsgenerator.extractor.article

trait ArticleExtractor {
  def extract(content: String): Option[String]
}
