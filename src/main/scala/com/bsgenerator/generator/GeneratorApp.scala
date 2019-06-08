package com.bsgenerator.generator

import com.bsgenerator.generator.lstm.{ArticleCharacterLevelIterator, LSTMGenerator}
import com.bsgenerator.model.Article
import com.bsgenerator.repository.{PostgresRepository, Repository}

import scala.util.matching.Regex

object GeneratorApp extends App {
  val generator: Generator = new LSTMGenerator
  val repository: Repository = new PostgresRepository
  val headerCleanup: Regex = "[!'\":.”?,-]".r
  val quote: Set[Char] = "˝˝ˮ»‘„«".toCharArray.toSet

  private def cleanupContent(content: String): String = {
    content.map {
      case c if quote.contains(c) => '"'
      case '\n' => ' '
      case c if !ArticleCharacterLevelIterator.validCharacters.contains(c) => '}' // FIXME
      case c => c
    }.replaceAll("\\s\\s+", " ").replace("}", "")
  }

  private def cleanupArticle(article: Article): Article = {
    Article(article.id, article.siteId, article.url, headerCleanup.replaceAllIn(article.header.trim(), ""), cleanupContent(article.content))
  }

  private def isActualArticle(article: Article): Boolean = {
    article.url.contains("/a/")
  }

  repository.init()
  try {
    val articles = repository.getArticles().filter(isActualArticle).map(cleanupArticle).toSet
    generator.train(articles)
  } finally {
    repository.cleanup()
  }
}
