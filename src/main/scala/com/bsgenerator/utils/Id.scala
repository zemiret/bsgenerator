package com.bsgenerator.utils

import java.util.UUID

trait IId {
  def randomId(): String
}

object Id extends IId {
  def randomId(): String = UUID.randomUUID.toString
}
