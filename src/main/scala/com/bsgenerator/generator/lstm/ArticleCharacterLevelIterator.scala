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
  def characterSet(): Set[Char] = {
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

}

class ArticleCharacterLevelIterator(batches: Int, batchLength: Int, articles: Seq[Article]) extends DataSetIterator {
  val validCharacters: List[Char] = ArticleCharacterLevelIterator.characterSet().toList
  val data: Seq[Char] = articles.flatMap(a => a.content + ' ')
  var pointers: Seq[Int] = preparePointers()
  var cp = 0

  private def preparePointers(): Seq[Int] = Random.shuffle(List.range(0, data.length - 1, batchLength))


  override def hasNext: Boolean = cp < pointers.size


  override def next(num: Int): DataSet = {
    val batchSize = Math.min(num, batches)
    val input = Nd4j.create(Array[Int](batchSize, validCharacters.length, batchLength), 'f')
    val labels = Nd4j.create(Array[Int](batchSize, validCharacters.length, batchLength), 'f')

    for (i <- 0 until batchSize) {
      val start = pointers(cp)
      val end = start + batchLength
      var charId = charToIdx(data(start))
      for (j <- start + 1 until end) {

        val next = charToIdx(data(j))
        input.putScalar(Array[Int](i, charId, j - 1 - start), 1.0)
        labels.putScalar(Array[Int](i, next, j - 1 - start), 1.0)
        charId = next
      }
    }
    cp += 1
    new DataSet(input, labels)
  }

  def idxToChar(idx: Int): Char = validCharacters(idx)

  def charToIdx(char: Char): Int = validCharacters.indexOf(char)

  override def reset(): Unit = {
    cp = 0
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
