package com.bsgenerator.extractor.article

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.mutable.Map
import scala.collection.JavaConverters._
import scala.collection.mutable

object HeuristicExtractor {

  val unlikelyClassCandidates: Array[String] = Array("-ad-", "ai2html", "banner", "breadcrumbs", "combx", "comment", "community", "cover-wrap", "disqus", "extra", "foot", "gdpr", "header", "legends", "menu", "related", "remark", "replies", "rss", "shoutbox", "sidebar", "skyscraper", "social", "sponsor", "supplemental", "ad-break", "agegate", "pagination", "pager", "popup", "yom-remote")
  val likelyClassCandidates: Array[String] = Array("article", "body", "column", "main")
  val minCharTextLength = 100

  val contentWrappers: Array[String] = Array("section", "div", "header", "img")

  val scorers: Seq[String => Int] = Seq(
    _.toCharArray.count(_ == ','),
    e => Math.min(3, e.length / 100)
  )

  val cleaningLadies: Seq[Element => Unit] = Seq(
    e => contentWrappers.map(e.getElementsByTag).filter(_.text().isBlank).foreach(_.remove)
  )

}

class HeuristicExtractor extends ArticleExtractor {

  import HeuristicExtractor._


  private def cleanContent(el: Element): Element = {
    cleaningLadies.foreach(_.apply(el))
    el
  }

  private def scoreParagraph(paragraph: String): Int = scorers.map(_.apply(paragraph)).sum

  private def scoreDivisionForLevel(level: Int): Int = {
    if (level > 1) {
      return level * 3
    }
    Math.max(1, level)
  }

  private def propagateScoring(paragraph: Element, scores: mutable.Map[Element, Int] = mutable.Map.empty): Unit = {
    val score = scoreParagraph(paragraph.text())
    paragraph.parents().asScala.toArray.zipWithIndex.foreach { case (p, i) =>
      scores.update(p, scores.getOrElse(p, 0) + (score / scoreDivisionForLevel(i)))
    }
  }

  private def canContainContent(el: Element): Boolean = {
    (
      (likelyClassCandidates.exists(el.className.contains(_)) || !unlikelyClassCandidates.exists(el.className.contains(_)))
        && el.text.trim.length > minCharTextLength)
  }

  private def getNonParagraphTextScoring(el: Element, depth: Int = 0): Int = {
    if (depth == 3) return 0
    val nonParagraphSiblings = el.siblingElements().asScala.filter(e => !(e.tagName().equalsIgnoreCase("p") || e.tagName().equalsIgnoreCase("pre")))

    (el.textNodes().asScala.map(e => scoreParagraph(e.text())).sum + nonParagraphSiblings.map(e => {
      getNonParagraphTextScoring(e, depth + 1)
    }).sum) / scoreDivisionForLevel(depth - 1)
  }

  override def extract(content: String): Option[String] = {
    val soup = Jsoup.parse(content)
    if (!soup.select("p, pre").asScala.toArray.map(_.parent()).toSet.exists(canContainContent)) {
      return null
    }
    val candidates = soup.select("p, pre").asScala.toArray.filter(canContainContent).map(cleanContent(_))
    val candidateScores: mutable.Map[Element, Int] = Map.empty
    candidates.foreach(e => propagateScoring(e, candidateScores))
    candidateScores.map {
      case (key, value) => value + getNonParagraphTextScoring(key)
    }

    candidateScores.toSeq.sortBy(_._2).lastOption match {
      case None => Option.empty
      case Some(e) => Option(e._1.wholeText().strip())
    }
  }
}