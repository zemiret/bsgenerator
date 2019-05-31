package com.bsgenerator.generator

import com.bsgenerator.model.Article

import scala.reflect.io.Path

trait Generator {
  def train(corpus: Set[Article])

  def generate(words: Int): String
//
//  def save(path: Path)
//
//  def load(path: Path)
}
