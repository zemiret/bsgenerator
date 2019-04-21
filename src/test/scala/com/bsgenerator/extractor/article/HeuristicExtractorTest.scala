package com.bsgenerator.extractor.article

import java.io.File

import org.scalatest.{Matchers, WordSpec, WordSpecLike}

import scala.io.Source

class HeuristicExtractorTest
  extends WordSpec
    with Matchers
    with WordSpecLike {

  "HeuristicStrategy" should {
    "extract content" in {
      val path = getClass.getResource(".")
      val folder = new File(path.getPath)
      folder.listFiles.toList.filter(_.isDirectory).foreach(testSite)
    }
  }

  def testSite(site: File): Unit = {
    val extractor = new HeuristicExtractor
    val source = Source.fromURL(getClass.getResource(s"${site.getName}/source.html"))
    val sourceContent = try extractor.extract(source.mkString) finally source.close()
    val expected = Source.fromURL(getClass.getResource(s"${site.getName}/expected"))
    val expectedContent = try expected.mkString finally expected.close()
    assertResult(expectedContent, site.getPath)(sourceContent.get)
  }
}
