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
  val characterSet = ArticleCharacterLevelIterator.characterSet()

  private def cleanupContent(content: String): String = {
    content.map(_ match {
      case c if quote.contains(c) => '"'
      case '\n' => ' '
      case c if !characterSet.contains(c) => '}' // TODO xd
      case c => c
    }).replaceAll("\\s\\s+", " ").replace("}", "")
  }

  private def cleanupArticle(article: Article): Article = {
    Article(article.id, article.siteId, article.url, headerCleanup.replaceAllIn(article.header.trim(), ""), cleanupContent(article.content))
  }

  private def isActualArticle(article: Article): Boolean = {
    return article.url.contains("/a/")
  }

  repository.init()
  try {
    val articles = repository.getArticles().filter(isActualArticle).map(cleanupArticle).toSet
//    print(articles.flatMap(art => art.content.toCharArray).filter(char => !characterSet.contains(char)).toSet)
    generator.train(articles)
  } finally {
    repository.cleanup()
  }
}
