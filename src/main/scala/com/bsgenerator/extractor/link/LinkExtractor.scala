package com.bsgenerator.extractor.link

trait LinkExtractor {
  def extract(content: String, context: String): Set[String]
}
