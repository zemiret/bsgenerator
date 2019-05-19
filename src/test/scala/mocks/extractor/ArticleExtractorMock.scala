package mocks.extractor

import com.bsgenerator.extractor.article.ArticleExtractor

class ArticleExtractorMock(retVal: String = "content") extends ArticleExtractor {
  override def extract(content: String): Option[String] = Option(retVal)
}
