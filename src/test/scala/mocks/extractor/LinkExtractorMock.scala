package mocks.extractor

import com.bsgenerator.extractor.link.LinkExtractor

class LinkExtractorMock(retVal: Set[String] = Set("url1", "url2", "url3")) extends LinkExtractor {
  override def extract(content: String, context: String): Set[String] = retVal
}
