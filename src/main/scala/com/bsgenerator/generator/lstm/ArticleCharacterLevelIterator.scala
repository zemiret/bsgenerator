package com.bsgenerator.generator.lstm

import java.util

import com.bsgenerator.model.Article
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.DataSetPreProcessor
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j

import scala.collection.mutable
import scala.util.Random

object ArticleCharacterLevelIterator {
  val validCharacters: Map[Char, Int] = characterSet().toList.zipWithIndex.toMap

  private def characterSet(): Set[Char] = {
    val chars = mutable.Set[Char]()
    // latin
    chars ++= ('a' to 'z')
    chars ++= ('A' to 'Z')
    chars ++= ('0' to '9')
    // polish
    chars += ('ą', 'ć', 'ę', 'ł', 'ń', 'ó', 'ś', 'ż', 'ź')
    chars += ('Ą', 'Ć', 'Ę', 'Ł', 'Ń', 'Ó', 'Ś', 'Ż', 'Ź')
    // punctuation
    chars += ('!', '&', '(', ')', '?', '-', '\'', '"', ',', '.', ':', ';', ' ', '\t')
    chars.toSet
  }

  def idxToChar(idx: Int): Char = validCharacters.find(_._2 == idx).get._1

  def charToIdx(char: Char): Int = validCharacters(char)

  def randomCharacter(rng: Random): Char = idxToChar(rng.nextInt(validCharacters.size))
}

class ArticleCharacterLevelIterator(batches: Int, batchLength: Int, articles: Seq[Article]) extends DataSetIterator {

  import ArticleCharacterLevelIterator._

  val data: Seq[Char] = articles.flatMap(a => a.content + ' ')
  var pointers: mutable.Queue[Seq[Char]] = preparePointers()

  private def preparePointers(): mutable.Queue[Seq[Char]] = {
    val groups = scala.util.Random.shuffle(data.grouped(batchLength).toList.dropRight(1))
    mutable.Queue(groups: _*)
  }


  override def hasNext: Boolean = !pointers.isEmpty


  override def next(num: Int): DataSet = {
    val batchSize = Math.min(num, pointers.size)
    val input = Nd4j.create(Array[Int](batchSize, validCharacters.size, batchLength), 'f')
    val labels = Nd4j.create(Array[Int](batchSize, validCharacters.size, batchLength), 'f')

    for (i <- 0 until batchSize) {
      val content = pointers.dequeue()
      for ((chars, j) <- content.sliding(2).zipWithIndex) {
        val charId = charToIdx(chars.head)
        val next = charToIdx(chars(1))
        input.putScalar(Array[Int](i, charId, j), 1.0)
        labels.putScalar(Array[Int](i, next, j), 1.0)
      }
    }
    new DataSet(input, labels)
  }

  override def reset(): Unit = {
    pointers = preparePointers()
  }

  override def inputColumns(): Int = validCharacters.size

  override def batch(): Int = batches

  override def setPreProcessor(preProcessor: DataSetPreProcessor): Unit = throw new UnsupportedOperationException("preprocessor not supported")

  override def getPreProcessor: DataSetPreProcessor = throw new UnsupportedOperationException("preprocessor not supported")

  override def getLabels: util.List[String] = throw new UnsupportedOperationException("labels not supported")

  override def totalOutcomes(): Int = validCharacters.size

  override def resetSupported(): Boolean = true

  override def asyncSupported(): Boolean = true

  override def next(): DataSet = next(batches)
}
