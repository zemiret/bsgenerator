package com.bsgenerator.extractor.link

import org.scalatest.{Matchers, WordSpec, WordSpecLike}

import scala.io.Source

class AttributeLinkExtractorTest
  extends WordSpec
    with Matchers
    with WordSpecLike {

  val basePath = "/com/bsgenerator/extractor"

  "LinkExtractor" should {

    "extract links from html source" in {
      val extractor = new AttributeLinkExtractor
      val source = Source.fromURL(getClass.getResource(basePath + "/stack-overflow-test.html")).mkString
      val links = extractor.extract(source, "https://stackoverflow.com/questions/7550376/how-can-sbt-pull-dependency-artifacts-from-git")
      assert(links contains "https://github.com/harrah/up")
      assert(links contains "https://stackoverflow.com")
    }

    "absolutize relative links" in {
      val extractor = new AttributeLinkExtractor
      val source = Source.fromURL(getClass.getResource(basePath + "/stack-overflow-test.html")).mkString
      val links = extractor.extract(source, "https://stackoverflow.com/questions/7550376/how-can-sbt-pull-dependency-artifacts-from-git")
      assert(links contains "https://stackoverflow.com/questions/ask")
      assert(!(links contains "/questions/ask"))
    }
  }
}
