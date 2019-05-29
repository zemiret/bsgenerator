package com.bsgenerator.generator

import com.bsgenerator.model.Article
import com.bsgenerator.repository.{PostgresRepository, Repository}

import scala.util.matching.Regex

object GeneratorApp extends App {
  val generator: Generator = new MarkovBasedHeaderGenerator
  val repository: Repository = new PostgresRepository
  val headerCleanup: Regex = "[!'\":.”?,-]".r

  private def cleanupArticle(article: Article): Article = {
    Article(article.id, article.siteId, article.url, headerCleanup.replaceAllIn(article.header.strip(), ""), article.content)
  }

  repository.init()
  try {
    val articles = repository.getArticles().filter(art => !art.header.toLowerCase.contains("frond")).map(cleanupArticle).toSet
    generator.train(articles)
    for (_ <- 1 to 20) {
      println(generator.generate(10))
    }
  } finally {
    repository.cleanup()
  }
}
