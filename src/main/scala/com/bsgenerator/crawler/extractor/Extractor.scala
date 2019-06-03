package com.bsgenerator.crawler.extractor

import akka.actor.{Actor, ActorLogging, Props}
import akka.dispatch.{BoundedMessageQueueSemantics, RequiresMessageQueue}
import com.bsgenerator.extractor.article.ArticleExtractor
import com.bsgenerator.extractor.header.HeaderExtractor
import com.bsgenerator.extractor.link.LinkExtractor

object Extractor {
  def props(headerExtractor: HeaderExtractor, articleExtractor: ArticleExtractor, linkExtractor: LinkExtractor): Props =
    Props(new Extractor(headerExtractor, articleExtractor, linkExtractor))

  final case class ExtractRequest(RequestId: String, url: String, content: String, baseUrl: String)
}

class Extractor(val headerExtractor: HeaderExtractor, val articleExtractor: ArticleExtractor, val linkExtractor: LinkExtractor)
  extends Actor with ActorLogging with RequiresMessageQueue[BoundedMessageQueueSemantics] {
  override def receive: Receive = {
    case Extractor.ExtractRequest(requestId, url, content, baseUrl) =>
      log.info("Extracting from url: {}", url)

      val extractedHeader = headerExtractor.extract(content)
      val extractedContent = articleExtractor.extract(content)
      val extractedLinks = linkExtractor.extract(content, baseUrl)

      sender() ! ExtractorCoordinator.ExtractedResponse(requestId, url, extractedHeader, extractedContent, extractedLinks)
  }
}
