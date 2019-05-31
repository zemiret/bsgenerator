package com.bsgenerator.generator

import com.bsgenerator.model.Article

import scala.collection.mutable
import scala.util.Random

/**
  * Mixed order markov model
  */
class MarkovBasedHeaderGenerator extends Generator {
  val model: mutable.Map[String, Set[String]] = mutable.Map()
  var random: Random = new Random

  implicit class SlidingOps[A](s: Array[A]) {
    def slidingTriples = (s, s.tail, s.tail.tail).zipped
  }

  override def train(corpus: Set[Article]): Unit = {
    corpus
      .map(_.header.split(' ') map (_.toLowerCase.trim))
      .filter(_.length > 3)
      .foreach(art => {
        art.slidingTriples.foreach {
          case (x1, x2, res) => {
            model.put(x1, model.getOrElse(x1, Set()) + x2)
            model.put(f"$x1 $x2", model.getOrElse(f"$x1 $x2", Set()) + res)
          }
        }
      })
  }

  override def generate(words: Int): String = {
    val starting = model.keySet.toList(random.nextInt(model.keySet.size))
    var sentence: Seq[String] = Seq(starting)
    for (_ <- 1 to words) {
      val right = sentence.takeRight(2)
      var next: Option[Set[String]] = Option.empty
      if (right.length > 1) {
        next = model.get(right.mkString(" "))
      }
      if (next.isEmpty) {
        next = model.get(right.last)
      }
      if (next.isEmpty) {
        return sentence.mkString(" ")
      }
      sentence = sentence :+ next.get.toList(random.nextInt(next.get.size))
    }
    sentence.mkString(" ")
  }
}
