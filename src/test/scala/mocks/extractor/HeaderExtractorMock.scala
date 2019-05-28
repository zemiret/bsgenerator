package mocks.extractor

import com.bsgenerator.extractor.header.HeaderExtractor

class HeaderExtractorMock(title: String) extends HeaderExtractor {
  override def extract(content: String): Option[String] = Option(title)
}
