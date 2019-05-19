package com.bsgenerator.crawler.extractor

import akka.actor.{Actor, ActorLogging, Props}
import com.bsgenerator.extractor.article.ArticleExtractor
import com.bsgenerator.extractor.link.LinkExtractor

object Extractor {
  def props(articleExtractor: ArticleExtractor, linkExtractor: LinkExtractor): Props =
    Props(new Extractor(articleExtractor, linkExtractor))

  final case class ExtractRequest(RequestId: String, content: String, baseUrl: String)
}

class Extractor(val articleExtractor: ArticleExtractor, val linkExtractor: LinkExtractor)
  extends Actor with ActorLogging {
  override def receive: Receive = {
    case Extractor.ExtractRequest(requestId, content, baseUrl) =>
      val extractedContent = articleExtractor.extract(content)
      val extractedLinks = linkExtractor.extract(content, baseUrl)

      sender() ! ExtractorCoordinator.ExtractedResponse(requestId, extractedContent, extractedLinks)
  }
}
