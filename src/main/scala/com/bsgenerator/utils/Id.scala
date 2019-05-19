package com.bsgenerator.utils

import java.util.UUID

object Id {
  def randomId(): String = UUID.randomUUID.toString
}
