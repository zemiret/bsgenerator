package com.bsgenerator.extractor.title

import java.io.File

import com.bsgenerator.extractor.header.OpenGraphTitleExtractor
import org.scalatest.{Matchers, WordSpec, WordSpecLike}

import scala.io.Source

class OpenGraphTitleExtractorTest
  extends WordSpec
    with Matchers
    with WordSpecLike {

  val basePath = "/com/bsgenerator/extractor/article"

  "LinkExtractor" should {

    "extract links from html source" in {
      val extractor = new OpenGraphTitleExtractor
      val source = Source.fromURL(getClass.getResource(basePath + "/gazeta-pl/source.html")).mkString
      val title = extractor.extract(source)
      assert(!title.isEmpty)
      assert(title.get.equals("Strajk trwa, a w poniedziałek rusza egzamin ósmoklasisty. CKE: Nie mamy informacji o problemach"))
    }
  }
}
