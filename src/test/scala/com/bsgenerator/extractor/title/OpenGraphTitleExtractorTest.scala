package com.bsgenerator.extractor.title

import com.bsgenerator.extractor.header.OpenGraphTitleExtractor
import org.scalatest.{Matchers, WordSpec, WordSpecLike}

import scala.io.Source

class OpenGraphTitleExtractorTest
  extends WordSpec
    with Matchers
    with WordSpecLike {

  val basePath = "/com/bsgenerator/extractor/article"

  "OpenGraphTitleExtractor" should {

    "extract header from og:title meta if present" in {
      val extractor = new OpenGraphTitleExtractor
      val source = Source.fromURL(getClass.getResource(basePath + "/gazeta-pl/source.html")).mkString
      val title = extractor.extract(source)
      assert(!title.isEmpty)
      assert(title.get.equals("Strajk trwa, a w poniedziałek rusza egzamin ósmoklasisty. CKE: Nie mamy informacji o problemach"))
    }

    "extract header from title element" in {
      val extractor = new OpenGraphTitleExtractor
      val source = Source.fromURL(getClass.getResource(basePath + "/mionskowski-pl/source.html")).mkString
      val title = extractor.extract(source)
      assert(!title.isEmpty)
      assert(title.get.equals("Using Google Apps Scripts for a simple backend - Maciej Mionskowski"))
    }
  }
}
