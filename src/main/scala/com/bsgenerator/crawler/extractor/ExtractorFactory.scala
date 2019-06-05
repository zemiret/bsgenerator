package com.bsgenerator.crawler.extractor

import com.bsgenerator.extractor.article.{ArticleExtractor, HeuristicExtractor}
import com.bsgenerator.extractor.header.{HeaderExtractor, OpenGraphTitleExtractor}
import com.bsgenerator.extractor.link.{AttributeLinkExtractor, LinkExtractor}

object ExtractorFactory {
  var HeaderExtractorCls: Class[_ <: HeaderExtractor] = classOf[OpenGraphTitleExtractor]
  var ArticleExtractorCls: Class[_ <: ArticleExtractor] = classOf[HeuristicExtractor]
  var LinkExtractorCls: Class[_ <: LinkExtractor] = classOf[AttributeLinkExtractor]


  def createExtractor() = Extractor.props(
    HeaderExtractorCls.newInstance(),
    ArticleExtractorCls.newInstance(),
    LinkExtractorCls.newInstance()
  )


  def setArticleExtractor(ExtractorCls: Class[_ <: ArticleExtractor]) = {
    ArticleExtractorCls = ExtractorCls
  }

  def setHeaderExtractor(ExtractorCls: Class[_ <: HeaderExtractor]) = {
    HeaderExtractorCls = ExtractorCls
  }

  def setLinkExtractor(ExtractorCls: Class[_ <: LinkExtractor]) = {
    LinkExtractorCls = ExtractorCls
  }
}
