package com.bsgenerator.extractor

import org.scalatest.FlatSpec

import scala.io.Source

class LinkExtractorTest extends FlatSpec {

  "LinkExtractor" should "extract links from html source" in {
    val extractor = new LinkExtractor
    val source = Source.fromURL(getClass.getResource("stack-overflow-test.html")).mkString
    val links = extractor.extract(source, "https://stackoverflow.com/questions/7550376/how-can-sbt-pull-dependency-artifacts-from-git")
    assert(links contains "https://github.com/harrah/up")
    assert(links contains "https://stackoverflow.com")
  }

  it should "absolutize relative links" in {
    val extractor = new LinkExtractor
    val source = Source.fromURL(getClass.getResource("stack-overflow-test.html")).mkString
    val links = extractor.extract(source, "https://stackoverflow.com/questions/7550376/how-can-sbt-pull-dependency-artifacts-from-git")
    assert(links contains "https://stackoverflow.com/questions/ask")
    assert(!(links contains "/questions/ask"))
  }

}
