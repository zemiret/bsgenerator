package com.bsgenerator.extractor.header

trait HeaderExtractor {
  def extract(content: String): Option[String]
}
